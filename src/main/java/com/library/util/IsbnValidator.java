package com.library.core.util;

/**
 * Утилита для валидации и форматирования ISBN
 */
public class IsbnValidator {

    /**
     * Проверяет валидность ISBN-10 или ISBN-13
     * @param isbn ISBN для проверки
     * @return true если ISBN валиден
     */
    public static boolean isValidIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return true; // null или пустая строка допустимы
        }

        // Удаляем все дефисы и пробелы
        String cleaned = isbn.replaceAll("[\\s-]+", "");

        // Проверяем длину
        int length = cleaned.length();

        if (length == 10) {
            return isValidIsbn10(cleaned);
        } else if (length == 13) {
            return isValidIsbn13(cleaned);
        }

        return false;
    }

    /**
     * Проверяет валидность ISBN-10
     */
    public static boolean isValidIsbn10(String isbn) {
        if (isbn == null || isbn.length() != 10) {
            return false;
        }

        // ISBN-10 должен содержать только цифры, последний символ может быть X (римская 10)
        if (!isbn.matches("[0-9]{9}[0-9X]")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            int digit = Character.getNumericValue(isbn.charAt(i));
            sum += digit * (10 - i);
        }

        char lastChar = isbn.charAt(9);
        int lastDigit = (lastChar == 'X' || lastChar == 'x') ? 10 : Character.getNumericValue(lastChar);
        sum += lastDigit;

        return sum % 11 == 0;
    }

    /**
     * Проверяет валидность ISBN-13
     */
    public static boolean isValidIsbn13(String isbn) {
        if (isbn == null || isbn.length() != 13) {
            return false;
        }

        // ISBN-13 должен содержать только цифры
        if (!isbn.matches("[0-9]{13}")) {
            return false;
        }

        // Проверяем префикс (должен быть 978 или 979)
        String prefix = isbn.substring(0, 3);
        if (!"978".equals(prefix) && !"979".equals(prefix)) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 13; i++) {
            int digit = Character.getNumericValue(isbn.charAt(i));
            int multiplier = (i % 2 == 0) ? 1 : 3; // Чередование 1 и 3
            sum += digit * multiplier;
        }

        return sum % 10 == 0;
    }

    /**
     * Преобразует ISBN-10 в ISBN-13
     */
    public static String convertIsbn10To13(String isbn10) {
        if (!isValidIsbn10(isbn10)) {
            throw new IllegalArgumentException("Invalid ISBN-10: " + isbn10);
        }

        // Удаляем последнюю цифру (контрольную сумму)
        String withoutCheckDigit = isbn10.substring(0, 9);

        // Добавляем префикс 978
        String isbn13Base = "978" + withoutCheckDigit;

        // Вычисляем новую контрольную сумму
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(isbn13Base.charAt(i));
            int multiplier = (i % 2 == 0) ? 1 : 3;
            sum += digit * multiplier;
        }

        int checkDigit = (10 - (sum % 10)) % 10;

        return isbn13Base + checkDigit;
    }

    /**
     * Форматирует ISBN в стандартный вид
     */
    public static String formatIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return isbn;
        }

        String cleaned = isbn.replaceAll("[\\s-]+", "");

        if (cleaned.length() == 10) {
            // Формат: X-XXXX-XXXX-X
            return cleaned.substring(0, 1) + "-" +
                    cleaned.substring(1, 4) + "-" +
                    cleaned.substring(4, 9) + "-" +
                    cleaned.substring(9);
        } else if (cleaned.length() == 13) {
            // Формат: XXX-X-XX-XXXXXX-X
            return cleaned.substring(0, 3) + "-" +
                    cleaned.substring(3, 4) + "-" +
                    cleaned.substring(4, 6) + "-" +
                    cleaned.substring(6, 12) + "-" +
                    cleaned.substring(12);
        }

        return isbn; // Если не соответствует формату, возвращаем как есть
    }

    /**
     * Проверяет, является ли строка ISBN-10
     */
    public static boolean isIsbn10(String isbn) {
        if (isbn == null) return false;
        String cleaned = isbn.replaceAll("[\\s-]+", "");
        return cleaned.length() == 10 && isValidIsbn10(cleaned);
    }

    /**
     * Проверяет, является ли строка ISBN-13
     */
    public static boolean isIsbn13(String isbn) {
        if (isbn == null) return false;
        String cleaned = isbn.replaceAll("[\\s-]+", "");
        return cleaned.length() == 13 && isValidIsbn13(cleaned);
    }

    /**
     * Нормализует ISBN (удаляет дефисы и пробелы, приводит к верхнему регистру для X)
     */
    public static String normalizeIsbn(String isbn) {
        if (isbn == null) return null;

        String cleaned = isbn.replaceAll("[\\s-]+", "");

        if (cleaned.length() == 10) {
            // Для ISBN-10 приводим последний символ к верхнему регистру, если это X
            char lastChar = cleaned.charAt(9);
            if (lastChar == 'x') {
                return cleaned.substring(0, 9) + 'X';
            }
        }

        return cleaned;
    }
}