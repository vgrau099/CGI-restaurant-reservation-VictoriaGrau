package com.cgi.restaurant.repository;

import com.cgi.restaurant.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TableRepository extends JpaRepository<RestaurantTable, Long> {

    List<RestaurantTable> findByTsoon(String tsoon); // SELECT * FROM lauad WHERE tsoon = ?

    List<RestaurantTable> findByMahtuvusGreaterThanEqual(int miinimumMahtuvus); // WHERE mahtuvus >= ?

    List<RestaurantTable> findByTsoonAndMahtuvusGreaterThanEqual(String tsoon, int miinimumMahtuvus); // WHERE tsoon = ? AND mahtuvus >= ?
}