package com.wrongweather.moipzy.domain.temperature;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TemperatureRepository extends JpaRepository<TemperatureRange, Long> {
}
