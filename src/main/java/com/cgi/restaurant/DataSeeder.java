package com.cgi.restaurant;

import com.cgi.restaurant.model.Reservation;
import com.cgi.restaurant.model.RestaurantTable;
import com.cgi.restaurant.repository.ReservationRepository;
import com.cgi.restaurant.repository.TableRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class DataSeeder implements CommandLineRunner { // Spring interface

    private final TableRepository laudadeRepo;
    private final ReservationRepository broneeringteRepo;
    private final Random juhuslik = new Random();

    public DataSeeder(TableRepository laudadeRepo, ReservationRepository broneeringteRepo) {
        this.laudadeRepo = laudadeRepo;
        this.broneeringteRepo = broneeringteRepo;
    }

    @Override
    public void run(String... args) {
        // ainult kui db on tühi
        if (laudadeRepo.count() == 0) {
            looLauad();
            looJuhuslikudBroneeringud();
        }
    }

    private void looLauad() {
        laudadeRepo.saveAll(List.of(
                looLaud("Laud 1", 2, "terrass", false,  true, true, false, 100, 90),
                looLaud("Laud 2", 2, "terrass", false,  false, true, false, 100, 210),
                looLaud("Laud 3", 4, "terrass", false,  false, true,  false, 100, 330),
                looLaud("Laud 4", 4, "terrass", false, false,  true, false, 100, 440),
                looLaud("Laud 5", 6, "terrass", false, true,  true, false, 100, 570),

                looLaud("Laud 6",  2, "siseala", true, true,  true, false, 340, 90),
                looLaud("Laud 7",  2, "siseala", false,  true, true, false, 560, 90),
                looLaud("Laud 8",  4, "siseala", true,  false, false, false, 340, 210),
                looLaud("Laud 9",  4, "siseala", false, false, false,  false, 560, 210),
                looLaud("Laud 10", 4, "siseala", true, false,  false, false, 340, 330),
                looLaud("Laud 11", 4, "siseala", false, false, false, false,  560, 330),
                looLaud("Laud 12", 6, "siseala", true, false, false, true,  340, 450),
                looLaud("Laud 13", 6, "siseala", false,  false, false, true, 560, 450),
                looLaud("Laud 14", 8, "siseala", true, true, true,  true, 340, 570),
                looLaud("Laud 15", 8, "siseala", false, true,  true, true, 560, 570),

                looLaud("Laud 16", 10, "privaatruum", true, true, true, false, 805, 330)
        ));
        System.out.println("---------- 16 lauda loodud ----------");
    }

    private RestaurantTable looLaud(
            String nimi, int mahtuvus, String tsoon,
            boolean aknaAares, boolean vaikneNurk,
            boolean ligipaasetav, boolean mangunurk,
            int x, int y) {

        RestaurantTable laud = new RestaurantTable();
        laud.setNimi(nimi);
        laud.setMahtuvus(mahtuvus);
        laud.setTsoon(tsoon);
        laud.setAknaAares(aknaAares);
        laud.setVaikneNurk(vaikneNurk);
        laud.setLigipaasetav(ligipaasetav);
        laud.setMangunurk(mangunurk);
        laud.setX(x);
        laud.setY(y);
        return laud;
    }

    private void looJuhuslikudBroneeringud() {
        // db-st kõik lauad
        List<RestaurantTable> koikLauad = laudadeRepo.findAll();

        // loob broneeringud järgmiseks 7 päevaks
        LocalDateTime tana = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);

        for (int paev = 0; paev < 7; paev++) {
            for (RestaurantTable laud : koikLauad) {
                int broneeringuteArv = juhuslik.nextInt(4);

                for (int i = 0; i < broneeringuteArv; i++) {
                    // 12:00 - 22:00
                    // algusaeg 12-20
                    int algustund = 12 + juhuslik.nextInt(9);
                    LocalDateTime algusAeg = tana.plusDays(paev).withHour(algustund);
                    LocalDateTime loppAeg = algusAeg.plusHours(2);

                    // kontrollib, kas see aeg on juba broneeritud
                    boolean kattub = !broneeringteRepo
                            .leiaKattuvadBroneeringud(laud, algusAeg, loppAeg)
                            .isEmpty();

                    // loob broneeringu
                    if (!kattub) {
                        Reservation broneering = new Reservation();
                        broneering.setLaud(laud);
                        broneering.setKliendiNimi(juhuslikkKliendiNimi());
                        broneering.setSeltskonnaSuurus(1 + juhuslik.nextInt(laud.getMahtuvus()));
                        broneering.setAlgusAeg(algusAeg);
                        broneering.setLoppAeg(loppAeg);
                        broneeringteRepo.save(broneering);
                    }
                }
            }
        }
        System.out.println("---------- Juhuslikud broneeringud loodud ----------");
    }

    // tagastab juhusliku eesti nime
    private String juhuslikkKliendiNimi() {
        String[] nimed = {
                "Mari Maasikas", "Victoria Grau", "Liisi Jõgi", "Peeter Oja",
                "Rando Seep", "Mart Sander", "Pippi Pikksukk", "Toomas Rand",
                "Eeva Esse", "Andres Rain", "Tiina Ojaste", "Raivo Tamm",
                "Jüri-Türi Üllar", "Emma Watson", "Karl-Erik Taukar"
        };
        return nimed[juhuslik.nextInt(nimed.length)];
    }
}