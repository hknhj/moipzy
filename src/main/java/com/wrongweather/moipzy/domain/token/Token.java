package com.wrongweather.moipzy.domain.token;

import com.wrongweather.moipzy.domain.users.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Token {

    @Id
    private int userId; // User의 Primary Key를 Token의 Primary Key로 사용

    @OneToOne
    @MapsId // User의 ID를 Token의 ID로 매핑
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "access_token", columnDefinition = "TEXT", nullable = false)
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT", nullable = false)
    private String refreshToken;

    @Builder
    public Token(User user, String accessToken, String refreshToken) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public void updateAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
