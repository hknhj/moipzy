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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

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

    }

    @Test
    void getAllOuter() throws Exception {

    }

    @Test
    void updateCloth() throws Exception {

    }

    @Test
    void deleteCloth() throws Exception {

    }
}
