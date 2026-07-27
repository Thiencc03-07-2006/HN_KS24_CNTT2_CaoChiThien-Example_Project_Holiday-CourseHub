package com.coursehub.service;

import com.coursehub.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanupExpiredAndRevokedTokens(UUID userId) {
        try {
            int deletedCount = refreshTokenRepository.deleteExpiredAndRevokedByUserId(userId);
            log.info("Successfully cleaned up {} old/revoked tokens for user: {}", deletedCount, userId);
        } catch (Exception ex) {
            log.warn("Failed to cleanup old tokens for user: {} due to: {}", userId, ex.getMessage());
        }
    }
}
