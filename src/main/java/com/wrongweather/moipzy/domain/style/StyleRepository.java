package com.wrongweather.moipzy.domain.style;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface StyleRepository extends JpaRepository<Style, Long> {

    Optional<Style> findByUser_UserIdAndWearAt(int userId, LocalDate wearAt);

    Optional<Style> findByStyleId(int styleId);

    Optional<Style> findByWearAt(LocalDate wearAt);
}
