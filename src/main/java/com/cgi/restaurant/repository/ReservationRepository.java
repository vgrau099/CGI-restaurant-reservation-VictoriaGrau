package com.cgi.restaurant.repository;

import com.cgi.restaurant.model.Reservation;
import com.cgi.restaurant.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // leia kõik broneeringud kindla laua jaoks
    List<Reservation> findByLaud(RestaurantTable laud);

    // leia broneeringud mis algavad antud ajavahemikus
    List<Reservation> findByLaudAndAlgusAegBetween(
            RestaurantTable laud,
            LocalDateTime algus,
            LocalDateTime lopp
    );

    // @Query annotatsiooni kasutamine - Spring Data JPA allikas:
    // https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.at-query
    // kattuvuse kontroll

    @Query("SELECT r FROM Reservation r WHERE r.laud = :laud AND r.algusAeg < :uusLopp AND r.loppAeg > :uusAlgus")
    List<Reservation> leiaKattuvadBroneeringud(
            @Param("laud") RestaurantTable laud,
            @Param("uusAlgus") LocalDateTime uusAlgus,
            @Param("uusLopp") LocalDateTime uusLopp
    );
}