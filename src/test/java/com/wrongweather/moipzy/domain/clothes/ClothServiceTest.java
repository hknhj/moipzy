//package com.wrongweather.moipzy.domain.clothes;
//
//import com.wrongweather.moipzy.domain.clothes.category.Color;
//import com.wrongweather.moipzy.domain.clothes.category.Degree;
//import com.wrongweather.moipzy.domain.clothes.category.LargeCategory;
//import com.wrongweather.moipzy.domain.clothes.category.SmallCategory;
//import com.wrongweather.moipzy.domain.clothes.dto.ClothIdResponseDto;
//import com.wrongweather.moipzy.domain.clothes.dto.ClothRegisterRequestDto;
//import com.wrongweather.moipzy.domain.clothes.dto.ClothResponseDto;
//import com.wrongweather.moipzy.domain.clothes.service.ClothService;
//import com.wrongweather.moipzy.domain.users.User;
//import com.wrongweather.moipzy.domain.users.UserRepository;
//import com.wrongweather.moipzy.domain.users.service.UserService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//public class ClothServiceTest {
//    @Autowired
//    private ClothService clothService;
//
//    @Autowired
//    private ClothRepository clothRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private UserService userService;
//
//    @Test
//    @Transactional
//    @Rollback(true)
//    @DisplayName("옷 등록 성공")
//    void 옷등록() {
//        //given
//        User user = userRepository.findByUserId(1).orElseThrow(() -> new RuntimeException());
//
//        ClothRegisterRequestDto clothRegisterRequestDto = ClothRegisterRequestDto.builder()
//                .userId(user.getUserId())
//                .largeCategory(LargeCategory.TOP)
//                .smallCategory(SmallCategory.TShirt)
//                .cloValue(0.09f)
//                .color(Color.BLUE)
//                .degree(Degree.FULL)
//                .build();
//
//        //when
//        ClothIdResponseDto clothIdResponseDto = clothService.registerCloth(clothRegisterRequestDto);
//
//        //then
//        Cloth cloth = clothRepository.findByClothId(clothIdResponseDto.getClothId()).orElseThrow(() -> new RuntimeException());
//
//        assertEquals(cloth.getClothId(), clothIdResponseDto.getClothId());
//    }
//
//    @Test
//    @Transactional
//    @Rollback(true)
//    @DisplayName("옷 단일 조회 성공")
//    void 옷단일조회() {
//        //given
//        User user = userRepository.findByUserId(1).orElseThrow(() -> new RuntimeException());
//
//        ClothRegisterRequestDto clothRegisterRequestDto = ClothRegisterRequestDto.builder()
//                .userId(user.getUserId())
//                .largeCategory(LargeCategory.TOP)
//                .smallCategory(SmallCategory.TShirt)
//                .cloValue(0.09f)
//                .color(Color.BLUE)
//                .degree(Degree.FULL)
//                .build();
//
//        ClothIdResponseDto clothIdResponseDto = clothService.registerCloth(clothRegisterRequestDto);
//
//        //when
//        ClothResponseDto clothResponseDto = clothService.getCloth(clothIdResponseDto.getClothId());
//
//        //then
//        assertEquals(clothResponseDto.getUserId(), user.getUserId());
//        assertEquals(clothResponseDto.getLargeCategory(), LargeCategory.TOP);
//        assertEquals(clothResponseDto.getSmallCategory(), SmallCategory.TShirt);
//        assertEquals(clothResponseDto.getCloValue(), 0.09f);
//        assertEquals(clothResponseDto.getColor(), Color.BLUE);
//        assertEquals(clothResponseDto.getDegree(), Degree.FULL);
//    }
//}
