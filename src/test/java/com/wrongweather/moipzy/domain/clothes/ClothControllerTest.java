package com.wrongweather.moipzy.domain.clothes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrongweather.moipzy.domain.clothImg.ClothImage;
import com.wrongweather.moipzy.domain.clothes.category.Color;
import com.wrongweather.moipzy.domain.clothes.category.Degree;
import com.wrongweather.moipzy.domain.clothes.category.LargeCategory;
import com.wrongweather.moipzy.domain.clothes.category.SmallCategory;
import com.wrongweather.moipzy.domain.clothes.controller.ClothController;
import com.wrongweather.moipzy.domain.clothes.dto.ClothRegisterRequestDto;
import com.wrongweather.moipzy.domain.clothes.dto.ClothResponseDto;
import com.wrongweather.moipzy.domain.clothes.dto.ClothUpdateRequestDto;
import com.wrongweather.moipzy.domain.clothes.exception.ClothNotFoundException;
import com.wrongweather.moipzy.domain.clothes.service.ClothService;
import com.wrongweather.moipzy.domain.exception.GlobalExceptionHandler;
import com.wrongweather.moipzy.domain.users.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClothController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ClothControllerTest {

    @MockBean
    private ClothService clothService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void uploadCloth() throws Exception {
        // given
        MockMultipartFile clothImg = new MockMultipartFile(
                "clothImg",
                "test-cloth.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image content".getBytes()
        );

        ClothRegisterRequestDto clothRegisterRequestDto = ClothRegisterRequestDto.builder()
                .userId(1)
                .largeCategory(LargeCategory.TOP)
                .smallCategory(SmallCategory.T_SHIRT)
                .color(Color.WHITE)
                .degree(Degree.NORMAL)
                .build();

        MockMultipartFile clothData = new MockMultipartFile(
                "clothData",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(clothRegisterRequestDto).getBytes()
        );

        given(clothService.registerCloth(any(MultipartFile.class), any(ClothRegisterRequestDto.class)))
                .willReturn(1);

        // when & then
        mockMvc.perform(multipart("/moipzy/clothes")
                        .file(clothImg)
                        .file(clothData)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().string("옷 등록 완료. Id : 1"));
    }

    @Test
    void getCloth() throws Exception {
        // given
        int userId = 1;
        int clothId = 10;

        // 유저 엔티티 생성
        User user = User.builder()
                .email("testemail@domain.com")
                .username("testUser")
                .password("password123")
                .build();
        user.setId(userId);

        // ClothImage 엔티티 생성
        ClothImage clothImage = ClothImage.builder()
                .imgUrl("http://test-image.com/cloth1.jpg")
                .build();

        // 옷 엔티티 생성
        Cloth cloth = Cloth.builder()
                .user(user)
                .clothImage(clothImage)
                .largeCategory(LargeCategory.OUTER)
                .smallCategory(SmallCategory.JACKET)
                .color(Color.BLACK)
                .degree(Degree.NORMAL)
                .highTemperature(25)
                .lowTemperature(15)
                .build();
        cloth.setClothId(clothId);

        ClothResponseDto clothResponseDto = ClothResponseDto.builder()
                .user(user)
                .cloth(cloth)
                .build();

        given(clothService.getCloth(userId, clothId)).willReturn(clothResponseDto);

        // when
        // then
        mockMvc.perform(get("/moipzy/clothes/{userId}/{clothId}", userId, clothId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.clothId").value(clothId))
                .andExpect(jsonPath("$.largeCategory").value("OUTER"))
                .andExpect(jsonPath("$.smallCategory").value("JACKET"))
                .andExpect(jsonPath("$.color").value("BLACK"))
                .andExpect(jsonPath("$.degree").value("NORMAL"))
                .andExpect(jsonPath("$.imgUrl").value("http://test-image.com/cloth1.jpg"));
    }

    @Test
    void getCloth_NotFound() throws Exception {
        // given
        int userId = 1;
        int clothId = 10;

        given(clothService.getCloth(userId, clothId))
                .willThrow(new ClothNotFoundException("Cloth with id " + clothId + " not found for user " + userId));

        // when
        // then
        mockMvc.perform(get("/moipzy/clothes/{userId}/{clothId}", userId, clothId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.message").value("Cloth with id 10 not found for user 1"));
    }

    @Test
    void getAllCloths() throws Exception {
        // given
        int userId = 1;

        User user = User.builder()
                .email("test@naver.com")
                .password("1234")
                .username("Nick")
                .build();
        user.setId(userId);

        List<ClothResponseDto> clothes = List.of(
                ClothResponseDto.builder()
                        .user(user)
                        .cloth(Cloth.builder()
                                .user(user)
                                .largeCategory(LargeCategory.TOP)
                                .smallCategory(SmallCategory.LONG_SLEEVE)
                                .color(Color.BLUE)
                                .degree(Degree.NORMAL)
                                .clothImage(ClothImage.builder()
                                        .imgUrl("/upload/clothes/image1")
                                        .build())
                                .build())
                        .build(),
                ClothResponseDto.builder()
                        .user(user)
                        .cloth(Cloth.builder()
                                .user(user)
                                .largeCategory(LargeCategory.OUTER)
                                .smallCategory(SmallCategory.BLOUSON)
                                .color(Color.NAVY)
                                .degree(Degree.NORMAL)
                                .clothImage(ClothImage.builder()
                                        .imgUrl("/upload/clothes/image2")
                                        .build())
                                .build())
                        .build(),
                ClothResponseDto.builder()
                        .user(user)
                        .cloth(Cloth.builder()
                                .user(user)
                                .largeCategory(LargeCategory.BOTTOM)
                                .smallCategory(SmallCategory.COTTON_PANTS)
                                .color(Color.BEIGE)
                                .degree(Degree.NORMAL)
                                .clothImage(ClothImage.builder()
                                        .imgUrl("/upload/clothes/image3")
                                        .build())
                                .build())
                        .build()
        );

        given(clothService.getAllClothes(userId)).willReturn(clothes);

        // when
        // then
        mockMvc.perform(get("/moipzy/clothes/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[1].userId").value(userId))
                .andExpect(jsonPath("$[2].userId").value(userId))
                .andExpect(jsonPath("$[0].largeCategory").value("TOP"))
                .andExpect(jsonPath("$[1].largeCategory").value("OUTER"))
                .andExpect(jsonPath("$[2].largeCategory").value("BOTTOM"));

        verify(clothService, times(1)).getAllClothes(userId);
    }

    @Test
    void updateCloth() throws Exception {
        // given
        int clothId = 1;
        ClothUpdateRequestDto clothUpdateRequestDto = ClothUpdateRequestDto.builder()
                .largeCategory(LargeCategory.OUTER)
                .smallCategory(SmallCategory.DENIM_JACKET)
                .color(Color.CHARCOAL)
                .degree(Degree.NORMAL)
                .build();

        given(clothService.updateCloth(eq(clothId), any(ClothUpdateRequestDto.class))).willReturn(clothId);

        // when
        // then
        mockMvc.perform(patch("/moipzy/clothes/{clothId}", clothId)
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clothUpdateRequestDto).getBytes()))
                .andExpect(status().isOk())
                .andExpect(content().string("옷 수정 완료. Id : " + clothId));

        verify(clothService, times(1)).updateCloth(eq(clothId), any(ClothUpdateRequestDto.class));

    }

    @Test
    void deleteCloth() throws Exception {
        // given
        int clothId = 1;

        given(clothService.deleteCloth(clothId)).willReturn(clothId);

        // when
        // then
        mockMvc.perform(delete("/moipzy/clothes/{clothId}", clothId))
                .andExpect(status().isOk())
                .andExpect(content().string("옷 삭제 완료. Id : " + clothId));

        verify(clothService, times(1)).deleteCloth(clothId);
    }
}
