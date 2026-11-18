package org.example.kukla;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.example.HtmlLoader;
import org.example.model.Performance;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class KirovKuklaParser {

    private static final String ITEM_SELECTOR = "div.spec-item";
    private static final String TITLE_SELECTOR = ".name a";
    private static final String DAY_SELECTOR = ".stroke-date .date";
    private static final String TIME_SELECTOR = ".start";
    private static final String AGE_SELECTOR = ".spec-age";
    private static final String IMAGE_SELECTOR = "img.spec-image";

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H.mm");
    private static final Pattern AGE_PATTERN = Pattern.compile("(\\d+)\\s*\\+");

    private final HtmlLoader loader;
    private final Map<String, String> imageCache = new HashMap<>();

    public KirovKuklaParser(final HtmlLoader loader) {
        this.loader = loader;
    }

    public List<Performance> parse(final Document fragment, final YearMonth month) {
        val result = new ArrayList<Performance>();

        for (Element item : fragment.select(ITEM_SELECTOR)) {
            try {
                result.add(parseItem(item, month));
            } catch (RuntimeException thrown) {
                log.warn("Не удалось разобрать спектакль за {}: {}", month, item.text(), thrown);
            }
        }
        return result;
    }

    private Performance parseItem(final Element item, final YearMonth month) {
        Element titleLink = item.selectFirst(TITLE_SELECTOR);
        Element dayElement = item.selectFirst(DAY_SELECTOR);
        Element timeElement = item.selectFirst(TIME_SELECTOR);
        if (titleLink == null || dayElement == null || timeElement == null) {
            throw new IllegalStateException("в блоке нет названия, дня или времени");
        }

        String title = titleLink.text().trim();

        int day = Integer.parseInt(dayElement.text().trim());
        LocalTime time = LocalTime.parse(timeElement.text().trim(), TIME_FORMAT);
        LocalDateTime startsAt = LocalDateTime.of(month.atDay(day), time);

        Integer ageRating = parseAge(item.selectFirst(AGE_SELECTOR));
        String imageUrl = loadImage(titleLink.absUrl("href"));

        // на сайте театра нет данных о продолжительности
        return new Performance(title, startsAt, null, ageRating, imageUrl);
    }

    private Integer parseAge(final Element ageElement) {
        if (ageElement == null) {
            return null;
        }
        Matcher matcher = AGE_PATTERN.matcher(ageElement.text());
        return matcher.find() ? Integer.valueOf(matcher.group(1)) : null;
    }

    private String loadImage(final String showUrl) {
        return imageCache.computeIfAbsent(showUrl, this::fetchImage);
    }

    private String fetchImage(final String showUrl) {
        try {
            Element img = loader.getSourceByUrl(showUrl).selectFirst(IMAGE_SELECTOR);
            return (img == null) ? "" : img.absUrl("src").replace(" ", "%20");
        } catch (IOException thrown) {
            log.warn("Не удалось загрузить страницу спектакля {}", showUrl, thrown);
            return "";
        }
    }
}