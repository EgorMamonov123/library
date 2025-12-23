package lab.library.enums;

public enum BookStatus {
    AVAILABLE,      // Доступна для выдачи
    BORROWED,       // Выдана
    RESERVED,       // Забронирована
    UNAVAILABLE,    // Недоступна (утеряна и т.д.)
    IN_PROCESSING   // В обработке
}