package com.library.util;

/**
 * Утилиты для работы со строками
 */
public class StringUtils {

    /**
     * Проверяет, является ли строка null или пустой/состоящей из пробелов
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Проверяет, не является ли строка null или пустой/состоящей из пробелов
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Обрезает строку до указанной длины, добавляя многоточие если нужно
     */
    public static String truncate(String str, int maxLength) {
        if (str == null || maxLength <= 0) {
            return str;
        }

        if (str.length() <= maxLength) {
            return str;
        }

        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Объединяет строки через разделитель, игнорируя null и пустые строки
     */
    public static String join(String delimiter, String... parts) {
        if (parts == null || parts.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (isNotBlank(part)) {
                if (sb.length() > 0) {
                    sb.append(delimiter);
                }
                sb.append(part);
            }
        }

        return sb.toString();
    }

    /**
     * Преобразует первую букву строки в верхний регистр
     */
    public static String capitalize(String str) {
        if (isBlank(str)) {
            return str;
        }

        return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
    }

    /**
     * Преобразует строку в camelCase
     */
    public static String toCamelCase(String str) {
        if (isBlank(str)) {
            return str;
        }

        String[] parts = str.split("[\\s_-]+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (isNotBlank(part)) {
                if (i == 0) {
                    result.append(part.toLowerCase());
                } else {
                    result.append(capitalize(part.toLowerCase()));
                }
            }
        }

        return result.toString();
    }

    /**
     * Удаляет все нецифровые символы из строки
     */
    public static String keepOnlyDigits(String str) {
        if (str == null) {
            return null;
        }

        return str.replaceAll("\\D+", "");
    }

    /**
     * Маскирует часть строки (например, для email или телефона)
     */
    public static String mask(String str, int visibleStart, int visibleEnd, char maskChar) {
        if (str == null || str.length() == 0) {
            return str;
        }

        if (visibleStart + visibleEnd >= str.length()) {
            return str; // Нечего маскировать
        }

        String start = str.substring(0, visibleStart);
        String end = str.substring(str.length() - visibleEnd);
        int maskLength = str.length() - visibleStart - visibleEnd;

        StringBuilder masked = new StringBuilder(start);
        for (int i = 0; i < maskLength; i++) {
            masked.append(maskChar);
        }
        masked.append(end);

        return masked.toString();
    }

    /**
     * Проверяет, содержит ли строка только буквы (без цифр и специальных символов)
     */
    public static boolean containsOnlyLetters(String str) {
        if (str == null) {
            return false;
        }

        return str.matches("[a-zA-Zа-яА-ЯёЁ\\s]+");
    }
}