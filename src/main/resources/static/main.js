// global muutujad
let kõikLauad = [];
let aktiivsedBroneeringud = [];
let vabadLauad = [];
let soovitusLaud = null;
let valitudLaud = null;
let canvas, ctx;
let otsinguAlgusAeg = null;
let otsinguLoppAeg = null;

const AVAMISAEG = 12;
const VIIMANE_BRONEERING = 20;
const MAX_PAEVI = 7;

// laua suurus canvasel
const LAUA_LAIUS = 100;
const LAUA_KORGUS = 65;

// --- KÄIVITAMINE ---

window.onload = async function () {
    canvas = document.getElementById('saaliPlaan');
    ctx = canvas.getContext('2d');

    seaCanvasSuurus();

    window.addEventListener('resize', () => {
        seaCanvasSuurus();
        joonistaSaaliPlaan();
    });

    const tana = new Date();
    document.getElementById('kuupaev').value = tana.toISOString().split('T')[0];
    document.getElementById('kellaaeg').value = '18:00';

    const maxKuupaev = new Date();
    maxKuupaev.setDate(maxKuupaev.getDate() + MAX_PAEVI);
    document.getElementById('kuupaev').min = tana.toISOString().split('T')[0];
    document.getElementById('kuupaev').max = maxKuupaev.toISOString().split('T')[0];

    // uuendab kellaajad
    uuendaKellaajad();
    document.getElementById('kuupaev').addEventListener('change', uuendaKellaajad);

    canvas.addEventListener('click', klikCanvasel);

    await laeKõikLauad();
    await laeAktiivsedBroneeringud();
    joonistaSaaliPlaan();
};

// uuendab kellaaja valikuid vastavalt kuupäevale
function uuendaKellaajad() {
    const kuupaev = document.getElementById('kuupaev').value;
    const kellaaegSelect = document.getElementById('kellaaeg');
    const tana = new Date();
    const tanaString = tana.toISOString().split('T')[0];
    const praeguneHour = tana.getHours();

    // keelab valida mineviku ajad kui valitud päev on tänane päev
    const options = kellaaegSelect.querySelectorAll('option');
    options.forEach(option => {
        const optionHour = parseInt(option.value.split(':')[0]);
        if (kuupaev === tanaString && optionHour <= praeguneHour) {
            option.disabled = true;
            option.style.color = '#444';
        } else {
            option.disabled = false;
            option.style.color = '';
        }
    });

    // kui hetkel valitud aeg on keelatud, valib järgmise vaba
    if (kellaaegSelect.selectedOptions[0].disabled) {
        const esimeneVaba = kellaaegSelect.querySelector('option:not([disabled])');
        if (esimeneVaba) kellaaegSelect.value = esimeneVaba.value;
    }
}

// kõrge DPI
function seaCanvasSuurus() {
    const konteiner = canvas.parentElement;
    const laius = konteiner.clientWidth;
    const korgus = konteiner.clientHeight - 60;

    // Canvas DPI skaleerimine retina ekraanide jaoks - leitud siit: https://web.dev/articles/canvas-hidipi
    // devicePixelRatio on ekraani pikslite tihedus
    const dpr = window.devicePixelRatio || 1;

    // seab canvas tegeliku suuruse suuremaks
    canvas.width = laius * dpr;
    canvas.height = korgus * dpr;

    // seab css suuruse normaalseks
    canvas.style.width = laius + 'px';
    canvas.style.height = korgus + 'px';

    // et joonistused oleksid õiges suuruses
    ctx.scale(dpr, dpr);

    // salvestab global
    canvas._cssLaius = laius;
    canvas._cssKorgus = korgus;
}

// ANDMETE LAADIMINE

async function laeKõikLauad() {
    try {
        const vastus = await fetch('/api/lauad');
        kõikLauad = await vastus.json();
    } catch (viga) {
        näitaTeadet('Laudade laadimine ebaõnnestus', 'viga');
    }
}

async function laeAktiivsedBroneeringud() {
    try {
        const vastus = await fetch('/api/broneeringud');
        aktiivsedBroneeringud = await vastus.json();
    } catch (viga) {
        näitaTeadet('Broneeringute laadimine ebaõnnestus', 'viga');
    }
}

// värskendab kõiki andmeid
async function värskendaAndmed() {
    vabadLauad = [];
    soovitusLaud = null;
    otsinguAlgusAeg = null;
    otsinguLoppAeg = null;
    valitudLaud = null;
    await laeKõikLauad();
    await laeAktiivsedBroneeringud();
    joonistaSaaliPlaan();
    näitaTeadet('Andmed värskendatud', 'edu');
}

// OTSING

async function otsiLaudu() {
    const kuupaev = document.getElementById('kuupaev').value;
    const kellaaeg = document.getElementById('kellaaeg').value;
    const inimesi = document.getElementById('inimesi').value;
    const tsoon = document.getElementById('tsoon').value;
    const aknaAares = document.getElementById('aknaAares').checked;
    const vaikneNurk = document.getElementById('vaikneNurk').checked;
    const ligipaasetav = document.getElementById('ligipaasetav').checked;
    const mangunurk = document.getElementById('mangunurk').checked;

    if (!kuupaev || !kellaaeg) {
        näitaTeadet('Palun vali kuupäev ja kellaaeg', 'viga');
        return;
    }

    const tunnid = parseInt(kellaaeg.split(':')[0]);
    if (tunnid < AVAMISAEG || tunnid > VIIMANE_BRONEERING) {
        näitaTeadet(`Broneerimine võimalik ${AVAMISAEG}:00 - ${VIIMANE_BRONEERING}:00`, 'viga');
        return;
    }

    // kontrollib, et ei broneerita minevikku
    const tana = new Date();
    const tanaString = tana.toISOString().split('T')[0];
    if (kuupaev === tanaString && tunnid <= tana.getHours()) {
        näitaTeadet('Valitud kellaaeg on minevikus', 'viga');
        return;
    }

    otsinguAlgusAeg = `${kuupaev}T${kellaaeg}:00`;
    const minutid = parseInt(kellaaeg.split(':')[1]);
    const loppTunnid = tunnid + 2;
    const loppAegStr = `${String(loppTunnid).padStart(2, '0')}:${String(minutid).padStart(2, '0')}`;
    otsinguLoppAeg = `${kuupaev}T${loppAegStr}:00`;

    try {
        // URLSearchParams päringustringi koostamiseks - allikas leitud siit: https://developer.mozilla.org/en-US/docs/Web/API/URLSearchParams
        const params = new URLSearchParams({
            algusAeg: otsinguAlgusAeg,
            loppAeg: otsinguLoppAeg,
            seltskonnaSuurus: inimesi,
            ...(tsoon && { tsoon })
        });

        const vabadVastus = await fetch(`/api/lauad/vabad?${params}`);
        vabadLauad = await vabadVastus.json();

        const soovitusParams = new URLSearchParams({
            algusAeg: otsinguAlgusAeg,
            loppAeg: otsinguLoppAeg,
            seltskonnaSuurus: inimesi,
            ...(tsoon && { tsoon }),
            aknaAares, vaikneNurk, ligipaasetav, mangunurk
        });

        const soovitusVastus = await fetch(`/api/lauad/soovitus?${soovitusParams}`);
        if (soovitusVastus.ok) {
            const soovitusTekst = await soovitusVastus.text();
            soovitusLaud = soovitusTekst ? JSON.parse(soovitusTekst) : null;
        }

        joonistaSaaliPlaan();

        if (vabadLauad.length === 0) {
            näitaTeadet('Valitud ajal vabu laudu ei leitud', 'viga');
        } else {
            näitaTeadet(`Leitud ${vabadLauad.length} vaba lauda`, 'edu');
        }

    } catch (viga) {
        näitaTeadet('Otsing ebaõnnestus', 'viga');
    }
}

// SAALIPLAANI JOONISTAMINE

function joonistaSaaliPlaan() {
    const l = canvas._cssLaius || canvas.width;
    const k = canvas._cssKorgus || canvas.height;

    ctx.clearRect(0, 0, l, k);
    joonistaTsoonid(l, k);
    kõikLauad.forEach(laud => joonistaLaud(laud, l, k));
}

function joonistaTsoonid(l, k) {
    // terrass vasakul - lilla
    ctx.fillStyle = 'rgba(155, 89, 182, 0.08)';
    ctx.fillRect(0, 0, l * 0.22, k);

    // siseala keskel - sinine
    ctx.fillStyle = 'rgba(52, 152, 219, 0.08)';
    ctx.fillRect(l * 0.22, 0, l * 0.56, k);

    // privaatruum paremal - lilla
    ctx.fillStyle = 'rgba(155, 89, 182, 0.08)';
    ctx.fillRect(l * 0.78, 0, l * 0.22, k);

    // mustad jooned
    ctx.strokeStyle = 'rgba(0, 0, 0, 0.8)';
    ctx.setLineDash([5, 5]);
    ctx.lineWidth = 1;

    ctx.beginPath();
    ctx.moveTo(l * 0.22, 0);
    ctx.lineTo(l * 0.22, k);
    ctx.stroke();

    ctx.beginPath();
    ctx.moveTo(l * 0.78, 0);
    ctx.lineTo(l * 0.78, k);
    ctx.stroke();

    ctx.setLineDash([]);

    ctx.font = 'bold 13px Ubuntu, sans-serif';
    ctx.textAlign = 'center';

    ctx.fillStyle = 'rgba(100, 50, 140, 0.9)';
    ctx.fillText('TERRASS', l * 0.11, 25);

    ctx.fillStyle = 'rgba(30, 90, 160, 0.9)';
    ctx.fillText('SISEALA', l * 0.50, 25);

    ctx.fillStyle = 'rgba(100, 50, 140, 0.9)';
    ctx.fillText('PRIVAATRUUM', l * 0.89, 25);
}

function joonistaLaud(laud, l, k) {
    const x = (laud.x / 900) * l;
    const y = (laud.y / 650) * k;

    let taustaVarv, ääreVarv;

    if (soovitusLaud && laud.id === soovitusLaud.id) {
        taustaVarv = '#e91e8c';
        ääreVarv = '#ff69b4';
    } else if (valitudLaud && laud.id === valitudLaud.id) {
        taustaVarv = '#3498db';
        ääreVarv = '#2980b9';
    } else if (onLaudVaba(laud)) {
        taustaVarv = '#27ae60';
        ääreVarv = '#2ecc71';
    } else {
        taustaVarv = '#c0392b';
        ääreVarv = '#e74c3c';
    }

    if (laud.tsoon === 'privaatruum') {
        // privaatruum ringi kujuline
        const raadius = 45;
        ctx.beginPath();
        ctx.arc(x, y, raadius, 0, Math.PI * 2); // Ringi joonistamine Canvas API-ga - allikas: // https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D/arc
        ctx.fillStyle = taustaVarv;
        ctx.fill();
        ctx.strokeStyle = ääreVarv;
        ctx.lineWidth = 2;
        ctx.stroke();

        ctx.fillStyle = '#fff';
        ctx.font = 'bold 12px Ubuntu, sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(laud.nimi, x, y - 6);

        ctx.font = '11px Ubuntu, sans-serif';
        ctx.fillStyle = 'rgba(255,255,255,0.85)';
        ctx.fillText(`${laud.mahtuvus} kohta`, x, y + 10);
    } else {
        // ristkülik teiste laudade jaoks
        ctx.beginPath();
        ctx.roundRect(x - LAUA_LAIUS/2, y - LAUA_KORGUS/2, LAUA_LAIUS, LAUA_KORGUS, 8); // Ümardatud nurkadega ristküliku joonistamine - allikas: https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D/roundRect
        ctx.fillStyle = taustaVarv;
        ctx.fill();
        ctx.strokeStyle = ääreVarv;
        ctx.lineWidth = 2;
        ctx.stroke();

        ctx.fillStyle = '#fff';
        ctx.font = 'bold 12px Ubuntu, sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(laud.nimi, x, y - 8);

        ctx.font = '11px Ubuntu, sans-serif';
        ctx.fillStyle = 'rgba(255,255,255,0.85)';
        ctx.fillText(`${laud.mahtuvus} kohta`, x, y + 8);
    }
}

function onLaudVaba(laud) {
    if (!otsinguAlgusAeg) return true;
    return vabadLauad.some(v => v.id === laud.id);
}

// CANVAS KLIKK

function klikCanvasel(e) {
    const rect = canvas.getBoundingClientRect();
    const klikX = e.clientX - rect.left;
    const klikY = e.clientY - rect.top;

    const l = canvas._cssLaius || canvas.width;
    const k = canvas._cssKorgus || canvas.height;

    for (const laud of kõikLauad) {
        const x = (laud.x / 900) * l;
        const y = (laud.y / 650) * k;

        let klikitud = false;

        if (laud.tsoon === 'privaatruum') {
            // ring
            const raadius = 45;
            const kaugus = Math.sqrt((klikX - x) ** 2 + (klikY - y) ** 2);
            klikitud = kaugus <= raadius;
        } else {
            // ristkülik
            klikitud = klikX >= x - LAUA_LAIUS/2 && klikX <= x + LAUA_LAIUS/2 &&
                       klikY >= y - LAUA_KORGUS/2 && klikY <= y + LAUA_KORGUS/2;
        }

        if (klikitud) {
            if (!onLaudVaba(laud) && otsinguAlgusAeg) {
                näitaTeadet('See laud on valitud ajal hõivatud', 'viga');
                return;
            }
            avaBroneeringuModal(laud);
            return;
        }
    }
}

// BRONEERINGU MODAL

function avaBroneeringuModal(laud) {
    valitudLaud = laud;

    const soovitusTekst = soovitusLaud && laud.id === soovitusLaud.id
        ? '<br>⭐ <strong>Soovitatud laud!</strong>' : '';

    document.getElementById('modalInfo').innerHTML = `
        <strong>${laud.nimi}</strong><br>
        Mahutavus: ${laud.mahtuvus} inimest<br>
        Tsoon: ${laud.tsoon}<br>
        ${laud.aknaAares ? '🪟 Akna ääres<br>' : ''}
        ${laud.vaikneNurk ? '🔇 Vaikne nurk<br>' : ''}
        ${laud.ligipaasetav ? '♿ Ligipääsetav<br>' : ''}
        ${laud.mangunurk ? '🧸 Mängunurga lähedal<br>' : ''}
        ${soovitusTekst}
    `;

    document.getElementById('modalTaust').classList.add('aktiivne');
    joonistaSaaliPlaan();
}

function sulgeModal() {
    document.getElementById('modalTaust').classList.remove('aktiivne');
    document.getElementById('kliendiNimi').value = '';
    valitudLaud = null;
    joonistaSaaliPlaan();
}

async function kinnitaBroneering() {
    const nimi = document.getElementById('kliendiNimi').value.trim();

    if (!nimi) {
        näitaTeadet('Palun sisesta oma nimi', 'viga');
        return;
    }

    if (!otsinguAlgusAeg) {
        näitaTeadet('Palun tee esmalt otsing', 'viga');
        return;
    }

    if (!valitudLaud) {
        näitaTeadet('Palun vali laud', 'viga');
        return;
    }

    const inimesi = document.getElementById('inimesi').value;

    try {
        const vastus = await fetch('/api/broneeringud', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                laudaId: String(valitudLaud.id),
                kliendiNimi: nimi,
                seltskonnaSuurus: String(inimesi),
                algusAeg: otsinguAlgusAeg,
                loppAeg: otsinguLoppAeg
            })
        });

        if (vastus.ok) {
            document.getElementById('modalTaust').classList.remove('aktiivne');
            document.getElementById('kliendiNimi').value = '';

            valitudLaud = null;
            vabadLauad = [];
            soovitusLaud = null;
            otsinguAlgusAeg = null;
            otsinguLoppAeg = null;

            await laeAktiivsedBroneeringud();
            joonistaSaaliPlaan();

            näitaTeadet(`Broneering kinnitatud! Tere tulemast, ${nimi}!`, 'edu');
        } else {
            const veaTekst = await vastus.text();
            näitaTeadet('Broneerimine ebaõnnestus: ' + veaTekst, 'viga');
        }
    } catch (viga) {
        näitaTeadet('Broneerimine ebaõnnestus', 'viga');
    }
}

// --- TEATED ---

function näitaTeadet(sõnum, tüüp) {
    const teadeEl = document.getElementById('teade');
    teadeEl.textContent = sõnum;
    teadeEl.className = `teade ${tüüp}`;
    teadeEl.style.display = 'block';
    setTimeout(() => {
        teadeEl.style.display = 'none';
    }, 3000);
}