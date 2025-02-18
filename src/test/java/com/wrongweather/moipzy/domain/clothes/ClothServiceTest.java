package com.wrongweather.moipzy.domain.clothes;

import com.wrongweather.moipzy.domain.clothImg.ClothImage;
import com.wrongweather.moipzy.domain.clothImg.ClothImageRepository;
import com.wrongweather.moipzy.domain.clothImg.service.ClothImgService;
import com.wrongweather.moipzy.domain.clothes.category.Color;
import com.wrongweather.moipzy.domain.clothes.category.Degree;
import com.wrongweather.moipzy.domain.clothes.category.LargeCategory;
import com.wrongweather.moipzy.domain.clothes.category.SmallCategory;
import com.wrongweather.moipzy.domain.clothes.dto.ClothRegisterRequestDto;
import com.wrongweather.moipzy.domain.clothes.dto.ClothResponseDto;
import com.wrongweather.moipzy.domain.clothes.dto.ClothUpdateRequestDto;
import com.wrongweather.moipzy.domain.clothes.exception.ClothNotFoundException;
import com.wrongweather.moipzy.domain.clothes.service.ClothService;
import com.wrongweather.moipzy.domain.users.User;
import com.wrongweather.moipzy.domain.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClothServiceTest {

    @InjectMocks
    private ClothService clothService;

    @Mock
    private ClothRepository clothRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClothImgService clothImgService;

    @Mock
    private ClothImageRepository clothImageRepository;

    @BeforeEach
    void setUp() {
        // 미리 생성된 유저
        User testUser = User.builder()
                .email("testuser@domain.com")
                .username("Test User")
                .password("encodedpassword")
                .build();
        testUser.setId(1);

        // Mock 동작 정의
        when(userRepository.findByUserId(1)).thenReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("옷 등록")
    void 옷등록() {
        //given
        User existingUser = userRepository.findByUserId(1).orElseThrow(() -> new RuntimeException());

        // MockMultipartFile 생성
        MockMultipartFile clothImg = new MockMultipartFile(
                "clothImg",
                "shirt.jpg",
                "image/jpeg",
                "dummy image content".getBytes()
        );

        // 옷 등록 요청 DTO
        ClothRegisterRequestDto clothRegisterRequestDto = ClothRegisterRequestDto.builder()
                .userId(existingUser.getUserId())
                .largeCategory(LargeCategory.TOP)
                .smallCategory(SmallCategory.T_SHIRT)
                .color(Color.BLUE)
                .degree(Degree.NORMAL)
                .build();

        // Mock ClothImage 저장 결과
        ClothImage savedClothImg = ClothImage.builder()
                .imgUrl("http://example.com/images/shirt.jpg")
                .build();
        savedClothImg.setClothImgId(1);

        // Mock Cloth 저장 결과
        Cloth cloth = Cloth.builder()
                .user(existingUser)
                .largeCategory(clothRegisterRequestDto.getLargeCategory())
                .smallCategory(clothRegisterRequestDto.getSmallCategory())
                .color(clothRegisterRequestDto.getColor())
                .degree(clothRegisterRequestDto.getDegree())
                .build();
        cloth.setClothId(1);

        // Mock 동작 정의
        when(clothImgService.uploadImage(clothImg)).thenReturn(savedClothImg);
        when(clothRepository.save(any(Cloth.class))).thenReturn(cloth);

        //when
        Integer clothId = clothService.registerCloth(clothImg, clothRegisterRequestDto);

        //then
        assertNotNull(clothId);
        verify(clothImgService, times(1)).uploadImage(clothImg);
        verify(clothRepository, times(1)).save(any(Cloth.class));
    }

    @Test
    @DisplayName("옷 단일 조회")
    void 옷단일조회() {
        //given
        User existingUser = userRepository.findByUserId(1).orElseThrow(() -> new RuntimeException());

        // 미리 생성된 옷 이미지
        ClothImage testImage = ClothImage.builder()
                .imgUrl("http://example.com/images/shirt.jpg")
                .build();
        testImage.setClothImgId(1);

        when(clothImageRepository.findById(1)).thenReturn(Optional.of(testImage));

        Cloth existingCloth = Cloth.builder()
                .user(existingUser)
                .largeCategory(LargeCategory.TOP)
                .smallCategory(SmallCategory.T_SHIRT)
                .color(Color.BLUE)
                .degree(Degree.NORMAL)
                .clothImage(clothImageRepository.findById(1).orElseThrow(() -> new RuntimeException()))
                .build();
        existingCloth.setClothId(1);

        given(clothRepository.findByUser_UserIdAndClothId(existingUser.getUserId(), existingCloth.getClothId())).willReturn(Optional.of(existingCloth));

        //when
        ClothResponseDto clothResponseDto = clothService.getCloth(existingUser.getUserId(), existingCloth.getClothId());

        //then
        assertEquals(clothResponseDto.getUserId(), existingUser.getUserId());
        assertEquals(clothResponseDto.getLargeCategory(), LargeCategory.TOP);
        assertEquals(clothResponseDto.getSmallCategory(), SmallCategory.T_SHIRT);
        assertEquals(clothResponseDto.getColor(), Color.BLUE);
        assertEquals(clothResponseDto.getDegree(), Degree.NORMAL);
    }

    @Test
    @DisplayName("옷 단일 조회 실패")
    void 옷단일조회실패() {
        // given
        User existingUser = userRepository.findByUserId(1).orElseThrow(() -> new RuntimeException());

        // 미리 생성된 옷 이미지
        ClothImage testImage = ClothImage.builder()
                .imgUrl("http://example.com/images/shirt.jpg")
                .build();
        testImage.setClothImgId(1);

        when(clothImageRepository.findById(1)).thenReturn(Optional.of(testImage));

        Cloth existingCloth = Cloth.builder()
                .user(existingUser)
                .largeCategory(LargeCategory.TOP)
                .smallCategory(SmallCategory.T_SHIRT)
                .color(Color.BLUE)
                .degree(Degree.NORMAL)
                .clothImage(clothImageRepository.findById(1).orElseThrow(() -> new RuntimeException()))
                .build();
        existingCloth.setClothId(1);

        given(clothRepository.findByUser_UserIdAndClothId(existingUser.getUserId(), existingCloth.getClothId() + 1)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clothService.getCloth(existingUser.getUserId(), existingCloth.getClothId() + 1))
                .isInstanceOf(ClothNotFoundException.class);

    }

    @Test
    @DisplayName("옷 전체 조회")
    void 옷전체조회() {
        //given
        User existingUser = userRepository.findByUserId(1).orElseThrow(() -> new RuntimeException());

        // 미리 생성된 옷 이미지
        ClothImage testImage = ClothImage.builder()
                .imgUrl("http://example.com/images/shirt.jpg")
                .build();
        testImage.setClothImgId(1);

        when(clothImageRepository.findById(1)).thenReturn(Optional.of(testImage));

        Cloth cloth1 = Cloth.builder()
                .user(existingUser)
                .largeCategory(LargeCategory.TOP)
                .smallCategory(SmallCategory.T_SHIRT)
                .color(Color.BLUE)
                .degree(Degree.NORMAL)
                .clothImage(clothImageRepository.findById(1).orElseThrow(() -> new RuntimeException()))
                .build();
        cloth1.setClothId(1);

        Cloth cloth2 = Cloth.builder()
                .user(existingUser)
                .largeCategory(LargeCategory.OUTER)
                .smallCategory(SmallCategory.BLAZER)
                .color(Color.BLACK)
                .degree(Degree.NORMAL)
                .clothImage(clothImageRepository.findById(1).orElseThrow(() -> new RuntimeException()))
                .build();
        cloth2.setClothId(2);

        Cloth cloth3 = Cloth.builder()
                .user(existingUser)
                .largeCategory(LargeCategory.BOTTOM)
                .smallCategory(SmallCategory.JEANS)
                .color(Color.BLUE)
                .degree(Degree.NORMAL)
                .clothImage(clothImageRepository.findById(1).orElseThrow(() -> new RuntimeException()))
                .build();
        cloth3.setClothId(3);

        given(clothRepository.findAllByUser_UserId(existingUser.getUserId())).willReturn(List.of(cloth1, cloth2, cloth3));

        //when
        List<ClothResponseDto> clothResponseDtoList = clothService.getAllClothes(existingUser.getUserId());

        //then
        assertEquals(3, clothResponseDtoList.size());

        for (ClothResponseDto clothResponseDto : clothResponseDtoList)
            assertEquals(clothResponseDto.getUserId(), existingUser.getUserId());

        assertEquals(LargeCategory.TOP, clothResponseDtoList.get(0).getLargeCategory());
        assertEquals(SmallCategory.T_SHIRT, clothResponseDtoList.get(0).getSmallCategory());
        assertEquals(Color.BLUE, clothResponseDtoList.get(0).getColor());
        assertEquals(Degree.NORMAL, clothResponseDtoList.get(0).getDegree());

        assertEquals(LargeCategory.OUTER, clothResponseDtoList.get(1).getLargeCategory());
        assertEquals(SmallCategory.BLAZER, clothResponseDtoList.get(1).getSmallCategory());
        assertEquals(Color.BLACK, clothResponseDtoList.get(1).getColor());
        assertEquals(Degree.NORMAL, clothResponseDtoList.get(1).getDegree());

        assertEquals(LargeCategory.BOTTOM, clothResponseDtoList.get(2).getLargeCategory());
        assertEquals(SmallCategory.JEANS, clothResponseDtoList.get(2).getSmallCategory());
        assertEquals(Color.BLUE, clothResponseDtoList.get(2).getColor());
        assertEquals(Degree.NORMAL, clothResponseDtoList.get(2).getDegree());
    }

    @Test
    @DisplayName("옷 대분류별 조회")
    void 대분류별조회() {
        //given
        User existingUser = userRepository.findByUserId(1).orElseThrow(() -> new RuntimeException());

        // 미리 생성된 옷 이미지
        ClothImage testImage = ClothImage.builder()
                .imgUrl("http://example.com/images/shirt.jpg")
                .build();
        testImage.setClothImgId(1);

        when(clothImageRepository.findById(1)).thenReturn(Optional.of(testImage));

        Cloth cloth1 = Cloth.builder()
                .user(existingUser)
                .largeCategory(LargeCategory.TOP)
                .smallCategory(SmallCategory.T_SHIRT)
                .color(Color.BLUE)
                .degree(Degree.NORMAL)
                .clothImage(clothImageRepository.findById(1).orElseThrow(() -> new RuntimeException()))
                .build();
        cloth1.setClothId(1);

        Cloth cloth2 = Cloth.builder()
                .user(existingUser)
                .largeCategory(LargeCategory.TOP)
                .smallCategory(SmallCategory.KNIT)
                .color(Color.BLACK)
                .degree(Degree.NORMAL)
                .clothImage(clothImageRepository.findById(1).orElseThrow(() -> new RuntimeException()))
                .build();
        cloth2.setClothId(2);

        Cloth cloth3 = Cloth.builder()
                .user(existingUser)
                .largeCategory(LargeCategory.TOP)
                .smallCategory(SmallCategory.POLO_SHIRT)
                .color(Color.BLUE)
                .degree(Degree.NORMAL)
                .clothImage(clothImageRepository.findById(1).orElseThrow(() -> new RuntimeException()))
                .build();
        cloth3.setClothId(3);

        given(clothRepository.findAllByLargeCategory(LargeCategory.TOP)).willReturn(List.of(cloth1, cloth2, cloth3));

        //when
        List<ClothResponseDto> clothResponseDtoList = clothService.getAllByLargeCategory(existingUser.getUserId(), "top");

        //then
        assertEquals(3, clothResponseDtoList.size());
        for (ClothResponseDto clothResponseDto : clothResponseDtoList) {
            assertEquals(clothResponseDto.getUserId(), existingUser.getUserId());
            assertEquals(clothResponseDto.getLargeCategory(), LargeCategory.TOP);
        }
    }

    @Test
    @DisplayName("옷 수정")
    void 옷수정() {
        // given
        int clothId = 1;
        User existingUser = userRepository.findByUserId(1).orElseThrow(() -> new RuntimeException());

        ClothUpdateRequestDto clothUpdateRequestDto = ClothUpdateRequestDto.builder()
                .largeCategory(LargeCategory.TOP)
                .smallCategory(SmallCategory.T_SHIRT)
                .color(Color.WHITE)
                .degree(Degree.NORMAL)
                .build();

        Cloth cloth = Cloth.builder()
                .user(existingUser)
                .largeCategory(LargeCategory.TOP)
                .smallCategory(SmallCategory.LONG_SLEEVE)
                .color(Color.BEIGE)
                .degree(Degree.NORMAL)
                .build();
        cloth.setClothId(clothId);

        given(clothRepository.findByClothId(clothId)).willReturn(Optional.of(cloth));

        // when
        int updatedClothId = clothService.updateCloth(clothId, clothUpdateRequestDto);

        // then
        assertThat(updatedClothId).isEqualTo(clothId);
        assertThat(cloth.getLargeCategory()).isEqualTo(LargeCategory.TOP);
        assertThat(cloth.getSmallCategory()).isEqualTo(SmallCategory.T_SHIRT);
        assertThat(cloth.getColor()).isEqualTo(Color.WHITE);
        assertThat(cloth.getDegree()).isEqualTo(Degree.NORMAL);

        verify(clothRepository).findByClothId(clothId);
    }

    @Test
    @DisplayName("옷 삭제")
    void 옷삭제() {
        // given
        int clothId = 1;
        User existingUser = userRepository.findByUserId(1).orElseThrow(() -> new RuntimeException());

        Cloth cloth = Cloth.builder()
                .user(existingUser)
                .largeCategory(LargeCategory.OUTER)
                .smallCategory(SmallCategory.BLAZER)
                .color(Color.BLACK)
                .degree(Degree.NORMAL)
                .build();
        cloth.setClothId(clothId);

        given(clothRepository.findByClothId(clothId)).willReturn(Optional.of(cloth));

        // when
        int deletedClothId = clothService.deleteCloth(clothId);

        // then
        assertThat(deletedClothId).isEqualTo(clothId);

        verify(clothRepository).delete(cloth);
        verify(clothRepository).findByClothId(clothId);
    }
}
