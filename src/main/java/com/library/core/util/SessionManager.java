package com.library.core.util;

import com.library.core.domain.user.User;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Менеджер сессий для управления активными пользователями
 */
public class SessionManager {

    private final Map<String, UserSession> activeSessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static final long SESSION_TIMEOUT_MINUTES = 30;
    private static final long CLEANUP_INTERVAL_MINUTES = 5;

    public SessionManager() {
        // Планировщик для очистки устаревших сессий
        scheduler.scheduleAtFixedRate(
                this::cleanupExpiredSessions,
                CLEANUP_INTERVAL_MINUTES,
                CLEANUP_INTERVAL_MINUTES,
                TimeUnit.MINUTES
        );
    }

    public String createSession(User user) {
        String sessionId = generateSessionId();
        UserSession session = new UserSession(user, System.currentTimeMillis());
        activeSessions.put(sessionId, session);
        return sessionId;
    }

    public Optional<User> validateSession(String sessionId) {
        UserSession session = activeSessions.get(sessionId);

        if (session == null) {
            return Optional.empty();
        }

        long currentTime = System.currentTimeMillis();
        long sessionAge = currentTime - session.getCreatedTime();

        if (sessionAge > SESSION_TIMEOUT_MINUTES * 60 * 1000) {
            activeSessions.remove(sessionId);
            return Optional.empty();
        }

        // Обновляем время последнего доступа
        session.setLastAccessTime(currentTime);
        return Optional.of(session.getUser());
    }

    public void invalidateSession(String sessionId) {
        activeSessions.remove(sessionId);
    }

    public void invalidateAllUserSessions(Long userId) {
        activeSessions.entrySet().removeIf(entry ->
                entry.getValue().getUser().getId().equals(userId)
        );
    }

    private void cleanupExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        long timeoutMillis = SESSION_TIMEOUT_MINUTES * 60 * 1000;

        activeSessions.entrySet().removeIf(entry -> {
            UserSession session = entry.getValue();
            long sessionAge = currentTime - session.getLastAccessTime();
            return sessionAge > timeoutMillis;
        });
    }

    private String generateSessionId() {
        return java.util.UUID.randomUUID().toString();
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    // Внутренний класс для хранения сессии
    private static class UserSession {
        private final User user;
        private final long createdTime;
        private long lastAccessTime;

        public UserSession(User user, long createdTime) {
            this.user = user;
            this.createdTime = createdTime;
            this.lastAccessTime = createdTime;
        }

        public User getUser() { return user; }
        public long getCreatedTime() { return createdTime; }
        public long getLastAccessTime() { return lastAccessTime; }
        public void setLastAccessTime(long lastAccessTime) {
            this.lastAccessTime = lastAccessTime;
        }
    }
}