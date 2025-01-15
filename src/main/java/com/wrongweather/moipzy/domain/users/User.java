package com.wrongweather.moipzy.domain.users;

import com.wrongweather.moipzy.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class User extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(length = 40)
    private String password;

    @Column(nullable = false, length = 30)
    private String username;

    @Column(name = "kakao_id", length = 100)
    private String kakaoId;

    public void updateKakaoId(String kakaoId) {
        this.kakaoId = kakaoId;
    }

    @Builder
    public User(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
    }

    public void setId(int userId) {
        this.userId = userId;
    }
}
