package com.coursehub.repository;

import com.coursehub.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
    Optional<PasswordResetTokenEntity> findByToken(String token);

    @Modifying
    @Query("UPDATE PasswordResetTokenEntity t SET t.used = true WHERE t.user.id = :userId")
    void invalidateAllByUserId(@Param("userId") java.util.UUID userId);
}
