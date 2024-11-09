package com.wrongweather.moipzy.domain.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenUtil {

    private final SecretKey secretKey;
    private final long expireTime = 1000 * 60 * 60;

    //해당 클래스 생성하면서 application.yml에 있는 jwt.secret 값을 넣어준다
    public JwtTokenUtil(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    //토큰 생성
    public JwtToken createToken(int userId, String email, String username) {

        //유저 정보에 대한 Claim 생성
        Claims claims = Jwts.claims();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("username", username);

        //생성된 Claim, 유효기간 등을 설정하고 accessToken 문자열 생성
        String accessToken =  Jwts.builder()
                .setSubject(Integer.toString(userId)+" user jwt")
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        //생성된 accessToken 문자열을 Jwt 에 넣어서 JwtToken 객체로 반환
        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .build();
    }

    public Claims extractClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

//    public String extractUserId(String token) {
//        // JWT 파싱 로직
//        Claims claims = Jwts.parserBuilder()
//                .setSigningKey(SECRET_KEY)
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//        return claims.get("userId", String.class);
//    }

//    public static boolean isExpired(String token, String secretKey) {
//        Date expiredDate = extractClaims(token).getExpiration();
//        return expiredDate.before(new Date());
//    }
}
