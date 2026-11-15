package org.example.spasskaya;

import lombok.extern.slf4j.Slf4j;
import org.example.HtmlLoader;
import org.example.Parser;
import org.example.model.Performance;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SpasskayaParser implements Parser<List<Performance>> {

    private static final String ROW_SELECTOR = "table.myafisha tr";
    private static final String MONTH_HEADER_SELECTOR = "h2";
    private static final String TITLE_SELECTOR = "a[href*=/afisha/show/]";
    private static final String DATE_SELECTOR = "a[href*=bilet]";
    private static final String AGE_SELECTOR = "span";
    private static final String DURATION_SELECTOR = "div.page_box div:contains(Продолжительность)";
    private static final Pattern HOURS_PATTERN = Pattern.compile("(\\d+)\\s*час");
    private static final Pattern MINUTES_PATTERN = Pattern.compile("(\\d+)\\s*мин");
    private static final String IMAGE_SELECTOR = "img.img_right";

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

    private record ShowDetails(Duration duration, String imageUrl) {}
    private final HtmlLoader loader;
    private final Map<String, ShowDetails> detailsCache = new HashMap<>();

    public SpasskayaParser(HtmlLoader loader) {
        this.loader = loader;
    }

    @Override
    public List<Performance> parse(Document document) {
        List<Performance> result = new ArrayList<>();
        YearMonth currentMonth = null;

        for (Element row : document.select(ROW_SELECTOR)) {
            Element header = row.selectFirst(MONTH_HEADER_SELECTOR);
            if (header != null) {
                currentMonth = parseMonth(header.text());
                continue;
            }
            try {
                Element titleLink = row.selectFirst(TITLE_SELECTOR);
                Element dateLink = row.selectFirst(DATE_SELECTOR);
                if (titleLink == null || dateLink == null) {
                    continue;
                }
                String title = titleLink.ownText().trim();

                Element ageSpan = titleLink.selectFirst(AGE_SELECTOR);
                Integer ageRating = (ageSpan == null) ? null : parseAge(ageSpan.text());

                LocalDateTime startsAt = parseStartsAt(dateLink.text(), currentMonth);
                if (startsAt == null) {
                    log.warn("Не удалось определить дату показа: {}", row.text());
                    continue;
                }

                String showUrl = titleLink.absUrl("href");
                ShowDetails details = loadDetails(showUrl);

                result.add(new Performance(title, startsAt, details.duration(), ageRating, details.imageUrl()));
            } catch (Exception e) {
                log.warn("Не удалось разобрать строку афиши: {}", row.text(), e);
            }
        }
        return result;
    }

    private Integer parseAge(String text) {
        String digits = text.replaceAll("\\D+", "");
        if (digits.isEmpty()) {
            log.warn("Не удалось распознать возрастное ограничение: '{}'", text);
            return null;
        }
        return Integer.parseInt(digits);
    }

    private LocalDateTime parseStartsAt(String text, YearMonth currentMonth) {

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
        } catch (NumberFormatException | DateTimeException e) {
            log.warn("Не удалось разобрать дату или время показа: '{}'", text, e);
            return null;
        }
    }


    private YearMonth parseMonth(String text) {
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
        } catch (NumberFormatException e) {
            log.warn("Год не удалось разобрать: '{}'", parts[1], e);
            return null;
        }
    }

    private ShowDetails fetchDetails(String showUrl) {
        try {
            Document page = loader.getSourceByUrl(showUrl);

            Element durationElement = page.selectFirst(DURATION_SELECTOR);
            Duration duration = (durationElement == null)
                    ? null : parseDuration(durationElement.text());

            Element imageLink = page.selectFirst(IMAGE_SELECTOR);
            String imageUrl = (imageLink == null) ? "" : imageLink.absUrl("src");

            return new ShowDetails(duration, imageUrl);
        } catch (IOException e) {
            log.warn("Не удалось загрузить страницу спектакля {}", showUrl, e);
            return new ShowDetails(null, "");
        }
    }

    private ShowDetails loadDetails(String showUrl) {
        return detailsCache.computeIfAbsent(showUrl, this::fetchDetails);
    }

    private Duration parseDuration(String text) {
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