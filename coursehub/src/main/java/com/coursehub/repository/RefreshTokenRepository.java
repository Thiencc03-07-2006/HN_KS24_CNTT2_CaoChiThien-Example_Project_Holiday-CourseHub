package com.coursehub.repository;

import com.coursehub.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByToken(String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rt FROM RefreshTokenEntity rt WHERE rt.token = :token")
    Optional<RefreshTokenEntity> findByTokenWithLock(@Param("token") String token);

    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.revoked = true WHERE rt.user.id = :userId")
    int revokeAllByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.revoked = true WHERE rt.token = :token")
    int revokeByToken(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.user.id = :userId AND (rt.revoked = true OR rt.expiryDate < CURRENT_TIMESTAMP)")
    int deleteExpiredAndRevokedByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.revoked = true OR rt.expiryDate < CURRENT_TIMESTAMP")
    int deleteAllExpiredAndRevoked();
}
