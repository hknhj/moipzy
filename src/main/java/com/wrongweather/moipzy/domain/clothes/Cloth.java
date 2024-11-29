package com.wrongweather.moipzy.domain.clothes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wrongweather.moipzy.domain.clothImg.ClothImage;
import com.wrongweather.moipzy.domain.clothes.category.Color;
import com.wrongweather.moipzy.domain.clothes.category.Degree;
import com.wrongweather.moipzy.domain.clothes.category.LargeCategory;
import com.wrongweather.moipzy.domain.clothes.category.SmallCategory;
import com.wrongweather.moipzy.domain.users.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class Cloth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int clothId;

    @ManyToOne(fetch = FetchType.LAZY) //유저 정보 거의 필요 없음
    @JoinColumn(name = "user_id")
    @JsonIgnore // 순환 참조 방지
    private User user;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cloth_img_id")
    private ClothImage clothImg;

    @Column(nullable = false, length = 50)
    @Enumerated(value = EnumType.STRING)
    private LargeCategory largeCategory;

    @Column(nullable = false, length = 50)
    @Enumerated(value = EnumType.STRING)
    private SmallCategory smallCategory;

    @Column(nullable = false, length = 30)
    @Enumerated(value = EnumType.STRING)
    private Color color;

    @Column(nullable = false, length = 30)
    @Enumerated(value = EnumType.STRING) //@Enumerated 작성안하면 indexOutOfRange 오류 발생
    private Degree degree;

    @Column(name="wear_at")
    @Setter
    private LocalDate wearAt;

    @Column(name="high_temp")
    @Setter
    private int highTemperature;

    @Column(name = "low_temp")
    @Setter
    private int lowTemperature;

    @Column(name = "solo_high_temp")
    @Setter
    private int soloHighTemperature;

    @Column(name = "solo_low_temp")
    @Setter
    private int soloLowTemperature;

    @Override
    public String toString() {
        String str = "Cloth {" +
                "clothId=" + clothId +
                ", largeCategory=" + largeCategory +
                ", smallCategory=" + smallCategory +
                ", color=" + color +
                ", degree=" + degree +
                ", wearAt=" + (wearAt != null ? wearAt.toString() : "null");

        if (this.largeCategory == LargeCategory.TOP) {
            str += ", soloHighTemperature=" + soloHighTemperature +
                    ", soloLowTemperature=" + soloLowTemperature;
        }

        str += '}';

        return str;
    }

    @Builder
    public Cloth(User user, ClothImage clothImage, LargeCategory largeCategory, SmallCategory smallCategory, Degree degree, Color color, int highTemperature, int lowTemperature, int soloHighTemperature, int soloLowTemperature) {
        this.user = user;
        this.clothImg = clothImage;
        this.largeCategory = largeCategory;
        this.smallCategory = smallCategory;
        this.degree = degree;
        this.color = color;
        this.highTemperature = highTemperature;
        this.lowTemperature = lowTemperature;
        this.soloHighTemperature = soloHighTemperature;
        this.soloLowTemperature = soloLowTemperature;
    }
}
