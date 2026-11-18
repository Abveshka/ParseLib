package org.parser;

import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class AfishaTextParser {
    private static final Pattern HOURS_PATTERN = Pattern.compile("(\\d+)\\s*час");
    private static final Pattern MINUTES_PATTERN = Pattern.compile("(\\d+)\\s*мин");

    private static final Map<String, Integer> MONTHS = Map.ofEntries(
            Map.entry("январь", 1),
            Map.entry("февраль", 2),
            Map.entry("март", 3),
            Map.entry("апрель", 4),
            Map.entry("май", 5),
            Map.entry("июнь", 6),
            Map.entry("июль", 7),
            Map.entry("август", 8),
            Map.entry("сентябрь", 9),
            Map.entry("октябрь", 10),
            Map.entry("ноябрь", 11),
            Map.entry("декабрь", 12)
    );

    public YearMonth parseMonth(final String text) {
        String[] parts = text.trim().toLowerCase().split("\\s+");
        if (parts.length != 2) {
            log.warn("Неожиданный формат заголовка месяца: '{}'", text);
            return null;
        }
        Integer month = MONTHS.get(parts[0]);
        if (month == null) {
            log.warn("Неизвестное название месяца: '{}'", parts[0]);
            return null;
        }
        try {
            String yearText = parts[1];
            int year = Integer.parseInt(yearText);
            return YearMonth.of(year, month);
        } catch (NumberFormatException thrown) {
            log.warn("Год не удалось разобрать: '{}'", parts[1], thrown);
            return null;
        }
    }

    public LocalDateTime parseStartsAt(final String text, final YearMonth currentMonth) {

        if (currentMonth == null) {
            return null;
        }

        String[] parts = text.trim().split("\\s+");
        if (parts.length < 4) {
            log.warn("Неверный формат даты и времени показа: '{}'", text);
            return null;
        }

        try {
            int day = Integer.parseInt(parts[0]);
            LocalTime time = LocalTime.parse(parts[parts.length - 1]);

            return currentMonth.atDay(day).atTime(time);
        } catch (NumberFormatException | DateTimeException thrown) {
            log.warn("Не удалось разобрать дату или время показа: '{}'", text, thrown);
            return null;
        }
    }

    public Integer parseAge(final String text) {
        String digits = text.replaceAll("\\D+", "");
        if (digits.isEmpty()) {
            log.warn("Не удалось распознать возрастное ограничение: '{}'", text);
            return null;
        }
        return Integer.parseInt(digits);
    }

    public Duration parseDuration(final String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String lowerText = text.toLowerCase(Locale.ROOT);
        long hours = 0;
        long minutes = 0;

        Matcher hoursMatcher = HOURS_PATTERN.matcher(lowerText);
        if (hoursMatcher.find()) {
            hours = Long.parseLong(hoursMatcher.group(1));
        }

        Matcher minutesMatcher = MINUTES_PATTERN.matcher(lowerText);
        if (minutesMatcher.find()) {
            minutes = Long.parseLong(minutesMatcher.group(1));
        }

        if (hours == 0 && minutes == 0) {
            log.warn("Не удалось распознать продолжительность спектакля: '{}'", text);
            return null;
        }

        return Duration.ofHours(hours).plusMinutes(minutes);
    }
}
