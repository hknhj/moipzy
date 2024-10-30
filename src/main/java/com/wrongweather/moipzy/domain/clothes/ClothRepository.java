package com.wrongweather.moipzy.domain.clothes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ClothRepository extends JpaRepository<Cloth, Long> {
    Optional<Cloth> findByClothId(int clothId);

    @Query(value = "SELECT * FROM cloth " +
            "WHERE (:outerId IS NULL OR cloth_id = :outerId) " +
            "OR (:semiOuterId IS NULL OR cloth_id = :semiOuterId) " +
            "OR (:topId IS NULL OR cloth_id = :topId) " +
            "OR (:bottomId IS NULL OR cloth_id = :bottomId)",
            nativeQuery = true)
    List<Cloth> findAllByOptionalIds(@Param("outerId") int outerId,
                                     @Param("semiOuterId") int semiOuterId,
                                     @Param("topId") int topId,
                                     @Param("bottomId") int bottomId);
}
