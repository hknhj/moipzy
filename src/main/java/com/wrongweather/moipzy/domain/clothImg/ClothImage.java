package com.wrongweather.moipzy.domain.clothImg;

import com.wrongweather.moipzy.domain.clothes.Cloth;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name="cloth_image")
@Getter
@Setter
@NoArgsConstructor
public class ClothImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cloth_img_id")
    private long clothImgId;

    @Column(name = "img_url", nullable = false)
    private String imgUrl;

    @OneToOne
    @JoinColumn(name="cloth_id", nullable = false)
    private Cloth cloth;

    @Builder
    public ClothImage(String imgUrl, Cloth cloth) {
        this.imgUrl = imgUrl;
        this.cloth = cloth;
    }
}
