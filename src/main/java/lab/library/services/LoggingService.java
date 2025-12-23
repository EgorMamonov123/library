package lab.library.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class LoggingService {

    /**
     * Логирование информационного сообщения
     */
    public void info(String message) {
        log.info("[{}] {}", LocalDateTime.now(), message);
    }

    /**
     * Логирование предупреждения
     */
    public void warn(String message) {
        log.warn("[{}] {}", LocalDateTime.now(), message);
    }

    /**
     * Логирование ошибки
     */
    public void error(String message, Throwable throwable) {
        log.error("[{}] {} - Error: {}", LocalDateTime.now(), message,
                throwable != null ? throwable.getMessage() : "No exception details");
    }

    /**
     * Логирование ошибки без исключения
     */
    public void error(String message) {
        log.error("[{}] {}", LocalDateTime.now(), message);
    }

    /**
     * Логирование отладки
     */
    public void debug(String message) {
        log.debug("[{}] {}", LocalDateTime.now(), message);
    }

    /**
     * Логирование важного действия (например, выдача/возврат книги)
     */
    public void logImportantAction(String action, String details) {
        String logMessage = String.format("IMPORTANT ACTION: %s | Details: %s", action, details);
        log.info("=".repeat(80));
        log.info(logMessage);
        log.info("=".repeat(80));
    }
}