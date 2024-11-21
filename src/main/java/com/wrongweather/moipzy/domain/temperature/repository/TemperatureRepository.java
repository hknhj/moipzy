package com.wrongweather.moipzy.domain.temperature.repository;

import com.wrongweather.moipzy.domain.temperature.TemperatureRange;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemperatureRepository extends JpaRepository<TemperatureRange, Long> {
}
