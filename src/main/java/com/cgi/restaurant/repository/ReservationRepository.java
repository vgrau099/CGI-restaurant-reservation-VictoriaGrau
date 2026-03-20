package com.cgi.restaurant.repository;

import com.cgi.restaurant.model.Reservation;
import com.cgi.restaurant.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // leiab kõik broneeringud
    List<Reservation> findByLaud(RestaurantTable laud);

    // leia broneeringud mis algavad antud ajavahemikus
    List<Reservation> findByLaudAndAlgusAegBetween(
            RestaurantTable laud,
            LocalDateTime algus,
            LocalDateTime lopp
    );

    // leia kõik broneeringud mis kattuvad antud ajavahemikuga
    List<Reservation> findByLaudAndAlgusAegLessThanAndLoppAegGreaterThan(
            RestaurantTable laud,
            LocalDateTime soovitudLopp,
            LocalDateTime soovitudAlgus
    );
}