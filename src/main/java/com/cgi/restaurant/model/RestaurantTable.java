package com.cgi.restaurant.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "lauad")
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nimi;          // nt Laud 1
    private int mahtuvus;         // mitu inimest lauda mahub
    private String tsoon;         // nt siseala, terrass või privaatruum
    private boolean aknaAares;
    private boolean vaikneNurk;
    private boolean ligipaasetav; // ratastooliga ligipääsetav?
    private boolean mangunurk;    // laste mängunurga lähedal?

    private int x; // asukoht saaliplaanil (pikslid)
    private int y;
}