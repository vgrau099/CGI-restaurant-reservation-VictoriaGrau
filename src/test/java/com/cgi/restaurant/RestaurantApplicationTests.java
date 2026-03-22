package com.cgi.restaurant;

import com.cgi.restaurant.model.RestaurantTable;
import com.cgi.restaurant.model.Reservation;
import com.cgi.restaurant.repository.TableRepository;
import com.cgi.restaurant.repository.ReservationRepository;
import com.cgi.restaurant.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // selleks, et testid ei segaks üksteist
class RestaurantApplicationTests {

    // Spring teeb automaatselt
    @Autowired
    private TableRepository laudadeRepo;

    @Autowired
    private ReservationRepository broneeringuteRepo;

    @Autowired
    private ReservationService reservationService;

    @Test
    void KasRakendusKaivitub() {
        assertTrue(true);
    }

    @Test
    void kontrolliLauaSalvestamist() {
        // loob uue laua
        RestaurantTable laud = new RestaurantTable();
        laud.setNimi("Testlaud 1");
        laud.setMahtuvus(4);
        laud.setTsoon("siseala");
        laud.setAknaAares(true);
        laud.setVaikneNurk(false);
        laud.setLigipaasetav(true);
        laud.setMangunurk(false);
        laud.setX(100);
        laud.setY(200);

        RestaurantTable salvestatud = laudadeRepo.save(laud);

        // kontrollib ID-d (kas salvestamine õnnestus)
        assertNotNull(salvestatud.getId());
        assertEquals("Testlaud 1", salvestatud.getNimi());
        assertEquals(4, salvestatud.getMahtuvus());
    }

    @Test
    void kontrolliTsooniOtsimist() {
        // loob kaks lauda eri tsoonides
        RestaurantTable siseala = new RestaurantTable();
        siseala.setNimi("Siseala laud");
        siseala.setMahtuvus(2);
        siseala.setTsoon("siseala");
        laudadeRepo.save(siseala);

        RestaurantTable terrass = new RestaurantTable();
        terrass.setNimi("Terrass laud");
        terrass.setMahtuvus(4);
        terrass.setTsoon("terrass");
        laudadeRepo.save(terrass);

        // otsib siseala laudi
        List<RestaurantTable> sisealaLauad = laudadeRepo.findByTsoon("siseala");

        // kas leiti vähemalt üks siseala laud
        assertFalse(sisealaLauad.isEmpty());
        // kas kõik leitud lauad on õiges tsoonis
        sisealaLauad.forEach(l -> assertEquals("siseala", l.getTsoon()));
    }

    @Test
    void kontrolliBroneeringuSalvestamist() {
        RestaurantTable laud = new RestaurantTable();
        laud.setNimi("Broneering testlaud");
        laud.setMahtuvus(4);
        laud.setTsoon("siseala");
        RestaurantTable salvestitudLaud = laudadeRepo.save(laud);

        // loob broneeringu sellele lauale
        Reservation broneering = new Reservation();
        broneering.setLaud(salvestitudLaud);
        broneering.setKliendiNimi("Mari Maasikas");
        broneering.setSeltskonnaSuurus(3);
        broneering.setAlgusAeg(LocalDateTime.of(2026, 3, 20, 18, 0));
        broneering.setLoppAeg(LocalDateTime.of(2026, 3, 20, 20, 0));

        // salvestab broneeringu
        Reservation salvestitudBroneering = broneeringuteRepo.save(broneering);

        // kas salvestamine õnnestus
        assertNotNull(salvestitudBroneering.getId());
        assertEquals("Mari Maasikas", salvestitudBroneering.getKliendiNimi());
        assertEquals(3, salvestitudBroneering.getSeltskonnaSuurus());
        // kas laua seos säilis
        assertEquals(salvestitudLaud.getId(), salvestitudBroneering.getLaud().getId());
    }

    @Test
    void kontrolliMahtuvuseOtsimist() {
        // loob lauad eri mahtuvustega
        RestaurantTable väikeLaud = new RestaurantTable();
        väikeLaud.setNimi("Väike laud");
        väikeLaud.setMahtuvus(2);
        väikeLaud.setTsoon("siseala");
        laudadeRepo.save(väikeLaud);

        RestaurantTable suurLaud = new RestaurantTable();
        suurLaud.setNimi("Suur laud");
        suurLaud.setMahtuvus(8);
        suurLaud.setTsoon("siseala");
        laudadeRepo.save(suurLaud);

        // leiab lauad, mis mahutavad vähemalt 4 inimest
        List<RestaurantTable> piisavaltSuured = laudadeRepo.findByMahtuvusGreaterThanEqual(4);

        // kontrollib, kas kõik leitud lauad mahutavad vähemalt 4 inimest
        assertTrue(piisavaltSuured.stream()
                .allMatch(l -> l.getMahtuvus() >= 4));
    }
    @Test
    void kontrolliKasLauadLuuakseSeedimisega() {
        // kas DataSeeder tegi 16 lauda?
        long laudadeArv = laudadeRepo.count();
        assertEquals(16, laudadeArv);
    }

    @Test
    void kontrolliKasBroneeringudLoodi() {
        // kas juhuslikud bron. loodi?
        long broneeringuteArv = broneeringuteRepo.count();
        assertTrue(broneeringuteArv > 0);
        System.out.println("Loodud broneeringuid kokku: " + broneeringuteArv);
    }

    @Test
    void kontrolliKasTsoonidOnOiged() {
        List<RestaurantTable> siseala = laudadeRepo.findByTsoon("siseala");
        List<RestaurantTable> terrass = laudadeRepo.findByTsoon("terrass");
        List<RestaurantTable> privaatruum = laudadeRepo.findByTsoon("privaatruum");

        assertEquals(10, siseala.size());
        assertEquals(5, terrass.size());
        assertEquals(1, privaatruum.size());
    }

    @Test
    void kasBroneeringudEiKattu() {
        // esimene laud
        RestaurantTable laud = laudadeRepo.findAll().get(0);

        // kõik selle laua bron.
        List<Reservation> broneeringud = broneeringuteRepo.findByLaud(laud);

        // kas ükski broneering ei kattu teisega?
        for (int i = 0; i < broneeringud.size(); i++) {
            for (int j = i + 1; j < broneeringud.size(); j++) {
                Reservation b1 = broneeringud.get(i);
                Reservation b2 = broneeringud.get(j);

                boolean kattub = b1.getAlgusAeg().isBefore(b2.getLoppAeg())
                        && b1.getLoppAeg().isAfter(b2.getAlgusAeg());

                assertFalse(kattub, "Broneeringud " + i + " ja " + j + " kattuvad!");
            }
        }
    }

    @Test
    void kasSeltskonnaSuurustOnMoistlik() {
        // ühegi broneeringu seltskonna suurus ei ületa laua mahtuvust?
        List<Reservation> koikBroneeringud = broneeringuteRepo.findAll();

        koikBroneeringud.forEach(broneering ->
                assertTrue(
                        broneering.getSeltskonnaSuurus() <= broneering.getLaud().getMahtuvus(),
                        "Seltskond on suurem kui laua mahutavus laual: " + broneering.getLaud().getNimi()
                )
        );
    }

    @Test
    void kasVabadLauadLeitakse() {
        // valib tuleviku aja
        LocalDateTime algus = LocalDateTime.now().plusDays(30).withHour(15).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime lopp = algus.plusHours(2);

        // leiab vabad lauad 2 inimesele (ilma tsoonita)
        List<RestaurantTable> vabadLauad = reservationService.leiaVabadLauad(algus, lopp, 2, null);

        assertFalse(vabadLauad.isEmpty(), "Peaks leidma vabu laudu 30 päeva pärast");
    }

    @Test
    void kasTsooniFiltreerimineTootab() {
        LocalDateTime algus = LocalDateTime.now().plusDays(30).withHour(15).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime lopp = algus.plusHours(2);

        // ainult terrassiga lauad
        List<RestaurantTable> terrassigaLauad = reservationService.leiaVabadLauad(algus, lopp, 2, "terrass");

        // kõik leitud lauad peavad olema terrassil
        terrassigaLauad.forEach(laud ->
                assertEquals("terrass", laud.getTsoon(), "Leitud laud ei ole terrassil")
        );
    }

    @Test
    void kontrolliSkoorimisalgoritmi() {
        RestaurantTable laud = new RestaurantTable();
        laud.setNimi("Testimine");
        laud.setMahtuvus(4);
        laud.setTsoon("siseala");
        laud.setAknaAares(true);
        laud.setVaikneNurk(false);
        laud.setLigipaasetav(false);
        laud.setMangunurk(false);

        // 2 inimest 4-kohalises lauas (2 tühja kohta) = -6 punkti
        // soovib akna ääres ja laud ON akna ääres = +10 punkti
        // kokku peaks olema 4 punkti
        int skoor = reservationService.arvutaSkoor(laud, 2, true, false, false, false);
        assertEquals(4, skoor);
    }

    @Test
    void kasSuuremSkoorVoidab() {
        RestaurantTable vaikeLaud = new RestaurantTable();
        vaikeLaud.setNimi("Väike");
        vaikeLaud.setMahtuvus(2); // 0 tühja kohta
        vaikeLaud.setTsoon("siseala");
        vaikeLaud.setAknaAares(false);
        vaikeLaud.setVaikneNurk(false);
        vaikeLaud.setLigipaasetav(false);
        vaikeLaud.setMangunurk(false);

        RestaurantTable suurLaud = new RestaurantTable();
        suurLaud.setNimi("Suur");
        suurLaud.setMahtuvus(8); // 6 tühja kohta = -18 punkti
        suurLaud.setTsoon("siseala");
        suurLaud.setAknaAares(false);
        suurLaud.setVaikneNurk(false);
        suurLaud.setLigipaasetav(false);
        suurLaud.setMangunurk(false);

        int vaikeLauaSkoor = reservationService.arvutaSkoor(vaikeLaud, 2, false, false, false, false);
        int suurLauaSkoor = reservationService.arvutaSkoor(suurLaud, 2, false, false, false, false);

        // väike laud peaks saama kõrgema skoori kui suur laud
        assertTrue(vaikeLauaSkoor > suurLauaSkoor,
                "Väike laud peaks olema eelistatum kui suur laud (2-liikmelisele seltskonnale)");
    }

    @Test
    void kontrolliBroneeringuLoomist() {
        // esimene laud
        RestaurantTable laud = laudadeRepo.findAll().get(0);

        LocalDateTime algus = LocalDateTime.now().plusDays(30).withHour(16).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime lopp = algus.plusHours(2);

        Reservation broneering = reservationService.looBreneering(
                laud.getId(), "Test Klient", 2, algus, lopp
        );

        // kas broneering loodi õigesti?
        assertNotNull(broneering.getId());
        assertEquals("Test Klient", broneering.getKliendiNimi());
        assertEquals(laud.getId(), broneering.getLaud().getId());
    }

    @Test
    void kasTopeltBroneeringEbaonnestub() {
        RestaurantTable laud = laudadeRepo.findAll().get(0);

        LocalDateTime algus = LocalDateTime.now().plusDays(31).withHour(16).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime lopp = algus.plusHours(2);

        reservationService.looBreneering(laud.getId(), "Esimene Klient", 2, algus, lopp);

        // teine broneering samale lauale samal ajal peaks ebaõnnestuma
        assertThrows(RuntimeException.class, () ->
                        reservationService.looBreneering(laud.getId(), "Teine Klient", 2, algus, lopp),
                "Topeltbroneering ei ebaõnnestunud"
        );
    }

    @Test
    void kontrolliTopeltBroneeringuidBackendis() {
        // esimene laud
        RestaurantTable laud = laudadeRepo.findAll().get(0);

        LocalDateTime algus = LocalDateTime.now().plusDays(30).withHour(14).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime lopp = algus.plusHours(2);

        Reservation esimene = reservationService.looBreneering(
                laud.getId(), "Test Klient 1", 2, algus, lopp
        );
        assertNotNull(esimene.getId(), "Esimene broneering peaks õnnestuma");

        // peab ebaõnnestuma
        assertThrows(RuntimeException.class, () ->
                reservationService.looBreneering(
                        laud.getId(), "Test Klient 2", 2, algus, lopp
                )
        );

        System.out.println("Backend topeltbroneeringu kaitse töötab");
    }
}