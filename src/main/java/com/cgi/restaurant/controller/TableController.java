package com.cgi.restaurant.controller;

import com.cgi.restaurant.model.RestaurantTable;
import com.cgi.restaurant.service.ReservationService;
import com.cgi.restaurant.repository.TableRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/lauad")
@CrossOrigin(origins = "*")
public class TableController {

    private final TableRepository laudadeRepo;
    private final ReservationService reservationService;

    public TableController(TableRepository laudadeRepo, ReservationService reservationService) {
        this.laudadeRepo = laudadeRepo;
        this.reservationService = reservationService;
    }

    // tagastab kõik lauad
    @GetMapping
    public List<RestaurantTable> koikLauad() {
        return laudadeRepo.findAll();
    }

    // tagastab vabad lauad filtrite järgi
    @GetMapping("/vabad")
    public List<RestaurantTable> vabadLauad(
            @RequestParam String algusAeg,
            @RequestParam String loppAeg,
            @RequestParam int seltskonnaSuurus,
            @RequestParam(required = false) String tsoon) {

        // frontendi jaoks parsib LocalDateTime
        LocalDateTime algus = LocalDateTime.parse(algusAeg);
        LocalDateTime lopp = LocalDateTime.parse(loppAeg);

        return reservationService.leiaVabadLauad(algus, lopp, seltskonnaSuurus, tsoon);
    }

    // tagastab eelistuste põhjal parima laua
    @GetMapping("/soovitus")
    public RestaurantTable soovitus(
            @RequestParam String algusAeg,
            @RequestParam String loppAeg,
            @RequestParam int seltskonnaSuurus,
            @RequestParam(required = false) String tsoon,
            @RequestParam(defaultValue = "false") boolean aknaAares,
            @RequestParam(defaultValue = "false") boolean vaikneNurk,
            @RequestParam(defaultValue = "false") boolean ligipaasetav,
            @RequestParam(defaultValue = "false") boolean mangunurk) {

        LocalDateTime algus = LocalDateTime.parse(algusAeg);
        LocalDateTime lopp = LocalDateTime.parse(loppAeg);

        return reservationService.leiaSobivaimLaud(algus, lopp, seltskonnaSuurus, tsoon,
                aknaAares, vaikneNurk, ligipaasetav, mangunurk);
    }
}