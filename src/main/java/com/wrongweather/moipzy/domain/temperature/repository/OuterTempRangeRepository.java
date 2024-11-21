package com.wrongweather.moipzy.domain.temperature.repository;

import com.wrongweather.moipzy.domain.temperature.OuterTempRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OuterTempRangeRepository extends JpaRepository<OuterTempRange, Integer> {

    @Query(value = """
        SELECT 
            CASE 
                WHEN :lowTemp > 28 THEN over28
                WHEN :lowTemp BETWEEN 24 AND 27 THEN between27_24
                WHEN :lowTemp BETWEEN 20 AND 23 THEN between23_20
                WHEN :lowTemp BETWEEN 17 AND 19 THEN between19_17
                WHEN :lowTemp BETWEEN 14 AND 16 THEN between16_14
                WHEN :lowTemp BETWEEN 11 AND 13 THEN between13_11
                WHEN :lowTemp BETWEEN 8 AND 10 THEN between10_8
                WHEN :lowTemp BETWEEN 5 AND 7 THEN between7_5
                ELSE under4
            END
        FROM OuterTempRange
        WHERE rangeId = :rangeId
    """, nativeQuery = true)
    String findOuterClothingForLowTemp(int lowTemp, int rangeId);
}
