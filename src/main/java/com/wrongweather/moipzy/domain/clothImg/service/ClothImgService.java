package com.wrongweather.moipzy.domain.clothImg.service;

import com.wrongweather.moipzy.domain.clothImg.ClothImage;
import com.wrongweather.moipzy.domain.clothImg.ClothImageRepository;
import com.wrongweather.moipzy.domain.clothes.Cloth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ClothImgService {

    private final ClothImageRepository clothImageRepository;

    public void uploadImage(MultipartFile image, Cloth cloth) {
        try {
            // 이미지 파일 저장을 위한 경로 설정
            String uploadsDir = "src/main/resources/static/uploads/clothes/";

            // 이미지 파일 경로를 저장
            String dbFilePath = saveImage(image, uploadsDir);

            // ProductThumbnail 엔티티 생성 및 저장
            ClothImage clothImage = ClothImage.builder()
                    .imgUrl(dbFilePath)
                    .cloth(cloth)
                    .build();
            clothImageRepository.save(clothImage);

        } catch (IOException e) {
            // 파일 저장 중 오류가 발생한 경우 처리
            e.printStackTrace();
        }
    }

    private String saveImage(MultipartFile image, String uploadDir) throws IOException {

        String fileName = java.util.UUID.randomUUID().toString().replace("-", "") + "_" + image.getOriginalFilename();
        String filePath = uploadDir + fileName;
        String dbFilePath = "/uploads/clothes/" + fileName;

        Path path = Paths.get(filePath); // Path 객체 생성
        Files.createDirectories(path.getParent()); // 디렉토리 생성
        Files.write(path, image.getBytes()); // 디렉토리에 파일 저장

        return dbFilePath;
    }
}