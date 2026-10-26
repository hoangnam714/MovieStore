package com.btlon.movie.store.repository;

import com.btlon.movie.store.entity.AuthToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    @EntityGraph(attributePaths = "user")
    Optional<AuthToken> findByTokenHash(String tokenHash);

    void deleteByTokenHash(String tokenHash);

    @Modifying
    @Query("DELETE FROM AuthToken t WHERE t.expiresAt < :now")
    void deleteExpired(LocalDateTime now);

    @Modifying
    @Query("DELETE FROM AuthToken t WHERE t.user.id = :userId")
    void deleteByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM AuthToken t WHERE t.user.id = :userId AND t.tokenHash <> :tokenHash")
    void deleteByUserIdAndTokenHashNot(Long userId, String tokenHash);
}
