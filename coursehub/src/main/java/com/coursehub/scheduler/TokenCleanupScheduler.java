package com.coursehub.scheduler;

import com.coursehub.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    // Run every day at midnight (UTC)
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupOldTokens() {
        log.info("Starting scheduled cleanup of expired and revoked refresh tokens...");
        try {
            int deletedCount = refreshTokenRepository.deleteAllExpiredAndRevoked();
            log.info("Scheduled cleanup finished. Deleted {} expired/revoked refresh tokens.", deletedCount);
        } catch (Exception e) {
            log.error("Error occurred during scheduled token cleanup: {}", e.getMessage(), e);
        }
    }
}
