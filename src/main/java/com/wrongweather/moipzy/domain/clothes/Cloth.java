package com.wrongweather.moipzy.domain.clothes;

import com.wrongweather.moipzy.domain.BaseTimeEntity;
import com.wrongweather.moipzy.domain.clothes.category.Degree;
import com.wrongweather.moipzy.domain.clothes.category.LargeCategory;
import com.wrongweather.moipzy.domain.clothes.category.SmallCategory;
import com.wrongweather.moipzy.domain.users.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Cloth extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int clothId;

    @ManyToOne(fetch = FetchType.LAZY) //유저 정보 거의 필요 없음
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 50)
    @Enumerated(value = EnumType.STRING)
    private LargeCategory largeCategory;

    @Column(nullable = false, length = 50)
    @Enumerated(value = EnumType.STRING)
    private SmallCategory smallCategory;

    @Column(nullable = false)
    private float cloValue;

    @Column(nullable = false, length = 30)
    private String color;

    @Column(nullable = false, length = 30)
    private Degree degree;

    @Builder
    public Cloth(User user, int cloValue, LargeCategory largeCategory, SmallCategory smallCategory, Degree degree, String color) {
        this.user = user;
        this.largeCategory = largeCategory;
        this.smallCategory = smallCategory;
        this.cloValue = cloValue;
        this.degree = degree;
        this.color = color;
    }
}
