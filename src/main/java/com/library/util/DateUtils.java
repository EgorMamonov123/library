package com.library.core.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Утилиты для работы с датами
 */
public class DateUtils {

    // Форматы дат
    public static final String DATE_FORMAT = "dd.MM.yyyy";
    public static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT);
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATE_TIME_FORMAT);

    /**
     * Форматирует LocalDate в строку
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    /**
     * Форматирует LocalDateTime в строку
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }

    /**
     * Форматирует LocalDate в ISO строку
     */
    public static String formatIsoDate(LocalDate date) {
        return date != null ? date.format(ISO_DATE_FORMATTER) : "";
    }

    /**
     * Форматирует LocalDateTime в ISO строку
     */
    public static String formatIsoDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(ISO_DATE_TIME_FORMATTER) : "";
    }

    /**
     * Парсит строку в LocalDate
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(dateString, ISO_DATE_FORMATTER);
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("Invalid date format: " + dateString +
                        ". Expected format: " + DATE_FORMAT + " or " + ISO_DATE_FORMAT);
            }
        }
    }

    /**
     * Парсит строку в LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(dateTimeString, ISO_DATE_TIME_FORMATTER);
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("Invalid datetime format: " + dateTimeString +
                        ". Expected format: " + DATE_TIME_FORMAT + " or " + ISO_DATE_TIME_FORMAT);
            }
        }
    }

    /**
     * Проверяет, находится ли дата в диапазоне
     */
    public static boolean isDateInRange(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null) return false;

        boolean afterStart = start == null || !date.isBefore(start);
        boolean beforeEnd = end == null || !date.isAfter(end);

        return afterStart && beforeEnd;
    }

    /**
     * Вычисляет количество дней между двумя датами
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return 0;
        }

        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Проверяет, является ли год високосным
     */
    public static boolean isLeapYear(int year) {
        return java.time.Year.of(year).isLeap();
    }

    /**
     * Получает возраст на указанную дату
     */
    public static int calculateAge(LocalDate birthDate, LocalDate onDate) {
        if (birthDate == null || onDate == null) {
            return 0;
        }

        return (int) ChronoUnit.YEARS.between(birthDate, onDate);
    }

    /**
     * Получает возраст на текущую дату
     */
    public static int calculateAge(LocalDate birthDate) {
        return calculateAge(birthDate, LocalDate.now());
    }

    /**
     * Добавляет рабочие дни к дате (без учета выходных)
     */
    public static LocalDate addBusinessDays(LocalDate date, int days) {
        if (date == null) return null;

        LocalDate result = date;
        int added = 0;

        while (added < days) {
            result = result.plusDays(1);
            // Пропускаем субботу (6) и воскресенье (7)
            if (result.getDayOfWeek().getValue() < 6) {
                added++;
            }
        }

        return result;
    }

    /**
     * Проверяет, является ли день рабочим
     */
    public static boolean isBusinessDay(LocalDate date) {
        if (date == null) return false;
        int dayOfWeek = date.getDayOfWeek().getValue();
        return dayOfWeek >= 1 && dayOfWeek <= 5; // Понедельник-пятница
    }

    /**
     * Получает следующую дату после указанного количества месяцев
     */
    public static LocalDate addMonthsSafely(LocalDate date, int months) {
        if (date == null) return null;

        try {
            return date.plusMonths(months);
        } catch (Exception e) {
            // Если добавление месяцев вызывает ошибку (например, 31 февраля),
            // возвращаем последний день месяца
            return date.plusMonths(months).withDayOfMonth(1).minusDays(1);
        }
    }
}