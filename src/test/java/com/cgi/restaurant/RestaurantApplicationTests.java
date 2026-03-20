package com.cgi.restaurant;

import com.cgi.restaurant.model.RestaurantTable;
import com.cgi.restaurant.model.Reservation;
import com.cgi.restaurant.repository.TableRepository;
import com.cgi.restaurant.repository.ReservationRepository;
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
        // kõik kolm tsooni on olemas?
        List<RestaurantTable> siseala = laudadeRepo.findByTsoon("siseala");
        List<RestaurantTable> terrass = laudadeRepo.findByTsoon("terrass");
        List<RestaurantTable> privaatruum = laudadeRepo.findByTsoon("privaatruum");

        assertEquals(8, siseala.size());
        assertEquals(5, terrass.size());
        assertEquals(3, privaatruum.size());
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
}