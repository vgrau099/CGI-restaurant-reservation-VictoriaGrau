package com.cgi.restaurant.service;

import com.cgi.restaurant.model.Reservation;
import com.cgi.restaurant.model.RestaurantTable;
import com.cgi.restaurant.repository.ReservationRepository;
import com.cgi.restaurant.repository.TableRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ReservationService {

    private final TableRepository laudadeRepo;
    private final ReservationRepository broneeringuteRepo;

    public ReservationService(TableRepository laudadeRepo, ReservationRepository broneeringuteRepo) {
        this.laudadeRepo = laudadeRepo;
        this.broneeringuteRepo = broneeringuteRepo;
    }

    public boolean onLaudVaba(RestaurantTable laud, LocalDateTime algusAeg, LocalDateTime loppAeg) {
        List<Reservation> kattuvadBroneeringud = broneeringuteRepo.leiaKattuvadBroneeringud(laud, algusAeg, loppAeg);
        System.out.println(">>> Kontrollime lauda: " + laud.getNimi());
        System.out.println(">>> Algus: " + algusAeg + " Lopp: " + loppAeg);
        System.out.println(">>> Kattuvaid broneeringuid: " + kattuvadBroneeringud.size());
        return kattuvadBroneeringud.isEmpty();
    }

    // leiab kõik vabad lauad filtritega
    public List<RestaurantTable> leiaVabadLauad(LocalDateTime algusAeg, LocalDateTime loppAeg, int seltskonnaSuurus, String tsoon) {
        List<RestaurantTable> kandidaadid;

        if (tsoon != null && !tsoon.isBlank()) {
            // kui tsoon on valitud
            kandidaadid = laudadeRepo.findByTsoonAndMahtuvusGreaterThanEqual(tsoon, seltskonnaSuurus);
        } else {
            kandidaadid = laudadeRepo.findByMahtuvusGreaterThanEqual(seltskonnaSuurus);
        }

        // filtreerib välja hõivatud lauad
        List<RestaurantTable> vabadLauad = new ArrayList<>();
        for (RestaurantTable laud : kandidaadid) {
            if (onLaudVaba(laud, algusAeg, loppAeg)) {
                vabadLauad.add(laud);
            }
        }
        return vabadLauad;
    }

    // arvutab laua skoori (sobivus)
    public int arvutaSkoor(RestaurantTable laud, int seltskonnaSuurus, boolean soovibAknaAares,
                           boolean soovibVaikust, boolean soovibLigipaasetavust, boolean soovibMangunurka) {

        int skoor = 0;

        // tühjad kohad -3
        int tyhjadKohad = laud.getMahtuvus() - seltskonnaSuurus;
        skoor -= tyhjadKohad * 3;

        // +10 eelistused
        if (soovibAknaAares && laud.isAknaAares()) skoor += 10;
        if (soovibVaikust && laud.isVaikneNurk()) skoor += 10;
        if (soovibLigipaasetavust && laud.isLigipaasetav()) skoor += 10;
        if (soovibMangunurka && laud.isMangunurk()) skoor += 10;

        return skoor;
    }

    // leiab parima laua
    public RestaurantTable leiaSobivaimLaud(LocalDateTime algusAeg, LocalDateTime loppAeg,
                                            int seltskonnaSuurus, String tsoon, boolean soovibAknaAares,
                                            boolean soovibVaikust, boolean soovibLigipaasetavust, boolean soovibMangunurka) {

        // leiab kõik vabad lauad
        List<RestaurantTable> vabadLauad = leiaVabadLauad(algusAeg, loppAeg, seltskonnaSuurus, tsoon);

        if (vabadLauad.isEmpty()) {
            return null;
        }

        // Kasutan Java Stream API max() meetodit koos Comparatoriga,
        // et leida suurima skooriga laud (õpitud siit allikalt: https://www.geeksforgeeks.org/java/stream-in-java/)
        return vabadLauad.stream()
                .max(Comparator.comparingInt(laud -> arvutaSkoor(
                        laud,
                        seltskonnaSuurus,
                        soovibAknaAares,
                        soovibVaikust,
                        soovibLigipaasetavust,
                        soovibMangunurka)))
                .orElse(null);
    }

    public Reservation looBreneering(Long laudaId, String kliendiNimi, int seltskonnaSuurus,
                                     LocalDateTime algusAeg, LocalDateTime loppAeg) {

        // leiab ID järgi
        RestaurantTable laud = laudadeRepo.findById(laudaId).orElseThrow(() ->
                new RuntimeException("Lauda ei leitud ID-ga: " + laudaId));

        if (!onLaudVaba(laud, algusAeg, loppAeg)) {
            throw new RuntimeException("Laud on valitud ajal juba broneeritud");
        }

        // loob ja salvestab broneeringu
        Reservation broneering = new Reservation();
        broneering.setLaud(laud);
        broneering.setKliendiNimi(kliendiNimi);
        broneering.setSeltskonnaSuurus(seltskonnaSuurus);
        broneering.setAlgusAeg(algusAeg);
        broneering.setLoppAeg(loppAeg);

        return broneeringuteRepo.save(broneering);
    }

    // tagastab kõik broneeringud
    public List<Reservation> koikBroneeringud() {
        return broneeringuteRepo.findAll();
    }
}