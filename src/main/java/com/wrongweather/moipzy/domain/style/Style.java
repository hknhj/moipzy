package com.wrongweather.moipzy.domain.style;

import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.users.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;


@Entity
@Getter
@NoArgsConstructor
public class Style {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int styleId;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id", referencedColumnName = "userId") //referenced에는 참조하고자 하는 클래스의 변수명을 넣는 것이다.
    private User user;

    @OneToOne
    @JoinColumn(name = "outer_id", referencedColumnName = "clothId")
    private Cloth outer;

    @OneToOne
    @JoinColumn(name = "semi_outer_id", referencedColumnName = "clothId")
    private Cloth semiOuter;

    @OneToOne
    @JoinColumn(name = "top_id", referencedColumnName = "clothId")
    private Cloth top;

    @OneToOne
    @JoinColumn(name = "bottom_id", referencedColumnName = "clothId")
    private Cloth bottom;

    @Column(name = "wear_at")
    private LocalDate wearAt;

    @Column(name = "feel_temp", nullable = false)
    private int feelTemp;

    @Builder
    public Style(User user, Cloth outer, Cloth semiOuter, Cloth top, Cloth bottom, int feelTemp) {
        this.user = user;
        this.outer = outer;
        this.semiOuter = semiOuter;
        this.top = top;
        this.bottom = bottom;
        this.feelTemp = feelTemp;
        this.wearAt = LocalDate.now();
    }
}
