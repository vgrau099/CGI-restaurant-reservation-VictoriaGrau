package com.cgi.restaurant.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "restaurant_tables")
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // Table 1
    private int peopleCount;    // how many people
    private String zone;        // indoor, terrace, private
    private boolean byWindow;   // by the window?
    private boolean quiet;      // quiet corner?
    private boolean accessible; // wheelchair accessibility?
    private boolean nearPlayArea; // near kids play area?

    private int x;  // pos on the floor plan
    private int y;
}