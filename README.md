# Nutikas Restorani Reserveerimissüsteem

CGI suvepraktika proovitöö – Victoria Grau

## Rakenduse käivitamine

### Eeldused
- Java 21+ installitud
- Git installitud

### Sammud
1. Kloonige repositoorium:
   https://github.com/vgrau099/CGI-restaurant-reservation-VictoriaGrau.git
2. Navigeerige projekti kausta:
   cd CGI-restaurant-reservation-VictoriaGrau
3. Käivitage rakendus Maven Wrapper abil:
   ./mvnw spring-boot:run
Windowsil:
   mvnw.cmd spring-boot:run
4. Käivitage RestaurantApplication.java
5. Avage brauser ja minge aadressile:
   http://localhost:8080


---

## Mis on ehitatud

Täisfunktsionaalne restorani reserveerimissüsteem, mis koosneb Spring Boot backendist ja HTML/CSS/JavaScript frontendist.

### Funktsionaalsus
- **Visuaalne saaliplaan** - restorani lauad on kuvatud interaktiivsel canvasel kolmes tsoonis: terrass, siseala ja privaatruum
- **Filtreerimine** - kasutaja saab filtreerida kuupäeva, kellaaja, seltskonna suuruse, tsooni ja eelistuste järgi
- **Laua soovitamine** - skoorimisalgoritm soovitab parimat lauda lähtuvalt seltskonna suurusest ja kliendi eelistustest (akna ääres, vaikne nurk, ligipääsetavus, mängunurga lähedus)
- **Broneeringute haldus** - broneeringud salvestatakse andmebaasi, topeltbroneeringud on välistatud
- **Juhuslikud broneeringud** - rakendus genereerib käivitumisel realistlikud broneeringud järgmiseks 7 päevaks
- **Automaattestid** - testid kontrollivad mudeleid, repositooriume, teenuseid ja broneeringute loogikat

### Tehniline ülevaade
- **Backend:** Spring Boot 3.5, Java 25, Spring Data JPA, H2 in-memory andmebaas
- **Frontend:** HTML, CSS, JavaScript, Canvas API
- **Ehitusvahend:** Maven
- **Versioonihaldus:** Git

---

## Tööle kulunud aeg

**Kokku:** ~4 päeva (neljapäev-pühapäev)

Enne kodeerimist kulus 1-3 päeva Spring Boot'i ja Maveni õppimisele, kuna polnud nendega varem töötanud.
---

## Ettevalmistus – Spring Boot ja Maven õppimine

Enne projekti alustamist tuli tutvuda Spring Boot'i ja Maveniga, kuna polnud nendega varem kokku puutunud. Vaatasin Spring Boot'i ametlikke õpetusi ning uurisin dokumentatsiooni aadressil [spring.io](https://spring.io/guides). Lisaks vaatasin YouTube'ist Spring Boot'i algõpetusi, et mõista, kuidas REST API-d, JPA ja annotatsioone kasutatakse.

**Maven vs Gradle:** Valisin Maveni, kuna soovisin proovida midagi uut - seni polnud ma Mavenit kasutanud. Spring Boot'i ametlik dokumentatsioon kasutab samuti Mavenit vaikevalikuna, mistõttu oli lihtsam õppematerjale leida.

Spring Boot'i õppimine enne koodimist osutus väga kasulikuks - eriti meeldisid mulle selle unikaalsed omadused nagu nt. automatic dependency injection, JPA repositooriumite automaatne genereerimine ainult liidese põhjal ja create-drop andmebaasistrateegia, mis eemaldas vajaduse SQL-skriptide kirjutamiseks täielikult. Nende asjade eelnev õppimine säästis arenduse käigus palju aega.

---

## Raskused ja lahendused

### 1. UTC ajavööndi probleem
Kõige rohkem aega võttis topeltbroneeringute vältimise vea leidmine. Väliselt tundus kõik korras, backend sai päringud kätte ja andmebaas salvestas broneeringuid, kuid sama lauda sai broneerida mitu korda samale ajale.

Debug-printide lisamisel selgus, et algusAeg ja loppAeg olid mõlemad 18:00, kuigi kasutaja valis 18:00 - 20:00. Probleem oli JavaScripti toISOString() meetodis, mis teisendab aja UTC-sse. Kuna arvuti on UTC+2 ajavööndis, lahutati 2 tundi maha – seega 20:00 sai 18:00. Kattuvuse kontroll leidis, et 18:00 < 18:00 on väär ja lubas topeltbroneeringu.

Lahendus oli loobuda Date objektist lõpuaja arvutamisel ja liita tunnid käsitsi stringina:
```javascript
const loppTunnid = tunnid + 2;
otsinguLoppAeg = `${kuupaev}T${String(loppTunnid).padStart(2, '0')}:00:00`;
```

### 2. Spring Data JPA päringu tõlgendamine
Kattuvuse kontrolliks kasutasin algselt Spring Data meetodit `findByLaudAndAlgusAegLessThanAndLoppAegGreaterThan`. Meetod genereeris õige SQL-i, kuid parameetrite järjekord oli segadusttekitav ja raskesti loetav. Asendasin selle selge `@Query` annotatsiooniga:
```java
@Query("SELECT r FROM Reservation r WHERE r.laud = :laud 
    AND r.algusAeg < :uusLopp AND r.loppAeg > :uusAlgus")
```
See muutis koodi loetavamaks ja eemaldas tõlgendamisvead.

### 3. Canvas DPI skaleerimine
kõrge DPI ekraanidel olid lauad hägused. Probleem oli selles, et canvas joonistati CSS-suuruses, kuid kõrge DPI ekraanidel on tegelik pikslite tihedus 2x suurem. Lahendus oli devicePixelRatio kasutamine:
```javascript
const dpr = window.devicePixelRatio || 1;
canvas.width = laius * dpr;
ctx.scale(dpr, dpr);
```

### 4. HTML struktuuriviga
Ühel hetkel kadusid lauad frontendilt täielikult. Pärast pikka otsimist selgus, et HTML-is oli kogemata kaks `<div class="legend">` opening tagi ilma ühe closing tagita, mis lõhkus kogu paigutuse ja canvas ei saanud õiget suurust.

---

## Eeldused ja otsused

- Broneeringu kestuseks on fikseeritud 2 tundi, kuna see on tüüpiline restoranikülastuse pikkus
- Restorani lahtiolekuaeg on 12:00–22:00, viimane broneering algab 20:00
- Broneerida saab maksimaalselt 7 päeva ette, kuna juhuslikud broneeringud genereeritakse samaks perioodiks
- Andmebaasina kasutasin H2 in-memory lahendust - see tähendab, et andmed lähtestuvad rakenduse taaskäivitamisel. Tootmiskeskkonnas oleks mõistlik kasutada PostgreSQL-i või MySQL-i

---

## AI ja muuda koodijuppide kasutamine

Kasutasin mõnikord Claude'i (Anthropic) abivahendina ning koodujuppe/õpetusi nt. https://spring.io/guides ja https://stackoverflow.com lehtedelt. Claude aitas:
- JavaScript Canvas API kasutamisel saaliplaanil
- Mõningate vigade otsimisel ja lahendamisel (eriti UTC ajavööndi probleem)
- natuke CSS kujundusega.

Kõik genereeritud koodiread on üle vaadatud, mõistetud ja kohandatud. Projekti käigus kõik mujalt saadud kood on mulle endale mõistetav.
---

## Lahendamata probleemid

- **Andmebaas lähtestub taaskäivitamisel** - H2 andmebaas kaotab andmed iga taaskäivitusega. Lahendus oleks kasutada püsivat andmebaasi nagu PostgreSQL

---
