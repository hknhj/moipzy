package com.wrongweather.moipzy.domain.style;

import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.style.dto.Feedback;
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
    @JoinColumn(name = "outer_id")
    private Cloth outer;

    @OneToOne
    @JoinColumn(name = "top_id")
    private Cloth top;

    @OneToOne
    @JoinColumn(name = "bottom_id")
    private Cloth bottom;

    @Column(name = "wear_at")
    private LocalDate wearAt;

    @Column(name = "highTemp", nullable = false)
    private int highTemp;

    @Column(name = "lowTemp", nullable = false)
    private int lowTemp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true) // 초기에는 피드백이 없을 수 있으므로 nullable 설정
    private Feedback feedback;

    public void updateStyle(Cloth outer, Cloth top, Cloth bottom) {
        this.outer = outer;
        this.top = top;
        this.bottom = bottom;
    }

    @Builder
    public Style(User user, Cloth outer, Cloth top, Cloth bottom, int highTemp, int lowTemp, Feedback feedback) {
        this.user = user;
        this.outer = outer;
        this.top = top;
        this.bottom = bottom;
        this.wearAt = LocalDate.now();
        this.highTemp = highTemp;
        this.lowTemp = lowTemp;
        this.feedback = feedback;
    }

    public void updateFeedback(Feedback feedback) {
        this.feedback = feedback;
    }
}
