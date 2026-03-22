package com.cgi.restaurant.controller;

import com.cgi.restaurant.model.Reservation;
import com.cgi.restaurant.service.ReservationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/broneeringud")
@CrossOrigin(origins = "*")
// http://localhost:8080/api/broneeringud
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // tagastab kõik broneeringud
    @GetMapping
    public List<Reservation> koikBroneeringud() {
        return reservationService.koikBroneeringud();
    }

    // loob uue broneeringu
    @PostMapping
    public ResponseEntity<?> looBreneering(@RequestBody Map<String, String> andmed) {
        try {
            Long laudaId = Long.parseLong(andmed.get("laudaId"));
            String kliendiNimi = andmed.get("kliendiNimi");
            int seltskonnaSuurus = Integer.parseInt(andmed.get("seltskonnaSuurus"));
            LocalDateTime algusAeg = LocalDateTime.parse(andmed.get("algusAeg"));
            LocalDateTime loppAeg = LocalDateTime.parse(andmed.get("loppAeg"));

            Reservation broneering = reservationService.looBreneering(
                    laudaId, kliendiNimi, seltskonnaSuurus, algusAeg, loppAeg);

            return ResponseEntity.ok(broneering);

        } catch (RuntimeException e) {
            // kui laud on juba broneeritud
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }
}