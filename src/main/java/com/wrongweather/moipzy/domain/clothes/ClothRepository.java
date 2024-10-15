package com.wrongweather.moipzy.domain.clothes;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClothRepository extends JpaRepository<Cloth, Long> {
    Optional<Cloth> findByClothId(int clothId);
}
