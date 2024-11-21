package com.wrongweather.moipzy.domain.temperature.repository;

import com.wrongweather.moipzy.domain.temperature.TopTempRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TopTempRangeRepository extends JpaRepository<TopTempRange, Long> {
    @Query(value = """
        SELECT 
            CASE 
                WHEN :highTemp > 28 THEN over28
                WHEN :highTemp BETWEEN 24 AND 27 THEN between27_24
                WHEN :highTemp BETWEEN 20 AND 23 THEN between23_20
                WHEN :highTemp BETWEEN 17 AND 19 THEN between19_17
                WHEN :highTemp BETWEEN 14 AND 16 THEN between16_14
                WHEN :highTemp BETWEEN 11 AND 13 THEN between13_11
                WHEN :highTemp BETWEEN 8 AND 10 THEN between10_8
                WHEN :highTemp BETWEEN 5 AND 7 THEN between7_5
                ELSE under4
            END
        FROM OuterTempRange
        WHERE rangeId = :rangeId
    """, nativeQuery = true)
    String findTopClothingForHighTemp(int highTemp, int rangeId);
}
