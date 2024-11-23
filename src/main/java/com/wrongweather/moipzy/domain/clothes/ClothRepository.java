package com.wrongweather.moipzy.domain.clothes;

import com.wrongweather.moipzy.domain.clothes.category.Degree;
import com.wrongweather.moipzy.domain.clothes.category.LargeCategory;
import com.wrongweather.moipzy.domain.clothes.category.SmallCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClothRepository extends JpaRepository<Cloth, Long> {
    Optional<Cloth> findByClothId(int clothId);

    List<Cloth> findAllByUser_UserId(int userId);

    List<Cloth> findAllByLargeCategory(LargeCategory largeCategory);

    List<Cloth> findAllBySmallCategory(SmallCategory smallCategory);

    @Query(value = "SELECT * FROM cloth " +
            "WHERE (:outerId IS NULL OR cloth_id = :outerId) " +
            "OR (:topId IS NULL OR cloth_id = :topId) " +
            "OR (:bottomId IS NULL OR cloth_id = :bottomId)", nativeQuery = true)
    List<Cloth> findAllByOptionalIds(@Param("outerId") int outerId,
                                     @Param("topId") int topId,
                                     @Param("bottomId") int bottomId);

    @Query(value = "SELECT c FROM Cloth c")
    List<Cloth> findAllCloths();

    @Query(value = "SELECT c FROM Cloth c WHERE c.smallCategory IN :categories AND c.degree = :degree")
    List<Cloth> findAllBySmallCategoryAndDegree(@Param("categories") List<SmallCategory> categories, Degree degree);

    @Query(value = "SELECT c FROM Cloth c WHERE c.degree IN :degrees AND c.largeCategory = :largeCategory")
        //동적쿼리를 사용할 때에는 nativeQuery=true를  추가해야한다.
    List<Cloth> findAllByDegreeAndLargeCategory(@Param("degrees") List<Degree> degrees, @Param("largeCategory") LargeCategory largeCategory);

    @Query("SELECT c FROM Cloth c WHERE :inputTemp BETWEEN c.lowTemperature AND c.highTemperature AND c.largeCategory = :largeCategory")
    List<Cloth> findByLargeCategoryAndTemperatureInRange(@Param("largeCategory") LargeCategory largeCategory, @Param("inputTemp") int inputTemp);
}
