package com.cgi.restaurant.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "laua_id")
    private RestaurantTable laud;

    private String kliendiNimi;
    private int seltskonnaSuurus;
    private LocalDateTime algusAeg;   // broneeringu algusaeg
    private LocalDateTime loppAeg;
}