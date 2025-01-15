package com.wrongweather.moipzy.domain.clothImg;

import com.wrongweather.moipzy.domain.clothes.Cloth;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name="cloth_image")
@Getter
@NoArgsConstructor
public class ClothImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cloth_img_id")
    private long clothImgId;

    @Column(name = "img_url", nullable = false)
    private String imgUrl;


    @Builder
    public ClothImage(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setClothImgId(long clothImgId) {
        this.clothImgId = clothImgId;
    }
}
