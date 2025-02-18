package com.wrongweather.moipzy.domain.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUserId(int userId);

    @Query("SELECT u.userId, u.kakaoId FROM User u WHERE u.kakaoId IS NOT NULL")
    List<Object[]> findUserAndKakaoIdForAllWithKakaoId();
}
