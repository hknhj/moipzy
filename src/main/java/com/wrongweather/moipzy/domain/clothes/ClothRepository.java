package com.wrongweather.moipzy.domain.clothes;

import com.wrongweather.moipzy.domain.clothes.category.LargeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClothRepository extends JpaRepository<Cloth, Long> {
    Optional<Cloth> findByClothId(int clothId);

    List<Cloth> findAllByUser_UserId(int userId);

    List<Cloth> findAllByLargeCategory(LargeCategory largeCategory);
}
