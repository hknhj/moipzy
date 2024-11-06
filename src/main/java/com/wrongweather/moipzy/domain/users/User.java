package com.wrongweather.moipzy.domain.users;

import com.wrongweather.moipzy.domain.BaseTimeEntity;
import com.wrongweather.moipzy.domain.temperature.TemperatureRange;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "range_id")
    private TemperatureRange range;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false, length = 40)
    private String password;

    @Column(nullable = false, length = 30)
    private String username;


    @Builder
    public User(String password, TemperatureRange range, String username, String email) {
        this.password = password;
        this.range = range;
        this.username = username;
        this.email = email;
    }

}
