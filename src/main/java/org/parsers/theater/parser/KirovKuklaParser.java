package org.parsers.theater.parser;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.parsers.common.html.HtmlLoader;
import org.parsers.theater.model.Performance;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
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

    HtmlLoader loader;
    Map<String, String> imageCache = new HashMap<>();

    public List<Performance> parse(@NonNull final Document fragment, @NonNull final YearMonth month) {
        val result = new ArrayList<Performance>();

        for (val item : fragment.select(ITEM_SELECTOR)) {
            try {
                result.add(parseItem(item, month));
            } catch (RuntimeException thrown) {
                log.warn("Не удалось разобрать спектакль за {}: {}", month, item.text(), thrown);
            }
        }
        return result;
    }

    private Performance parseItem(@NonNull final Element item, @NonNull final YearMonth month) {
        val titleLink = item.selectFirst(TITLE_SELECTOR);
        val dayElement = item.selectFirst(DAY_SELECTOR);
        val timeElement = item.selectFirst(TIME_SELECTOR);
        if (titleLink == null || dayElement == null || timeElement == null) {
            throw new IllegalStateException("в блоке нет названия, дня или времени");
        }

        val title = titleLink.text().trim();

        val day = Integer.parseInt(dayElement.text().trim());
        val time = LocalTime.parse(timeElement.text().trim(), TIME_FORMAT);
        val startsAt = LocalDateTime.of(month.atDay(day), time);

        val ageRating = parseAge(item.selectFirst(AGE_SELECTOR));
        val imageUrl = loadImage(titleLink.absUrl("href"));

        // на сайте театра нет данных о продолжительности
        return new Performance(title, startsAt, null, ageRating, imageUrl);
    }

    private Integer parseAge(@Nullable final Element ageElement) {
        if (ageElement == null) {
            return null;
        }
        val matcher = AGE_PATTERN.matcher(ageElement.text());
        return matcher.find()
            ? Integer.valueOf(matcher.group(1))
            : null;
    }

    private String loadImage(@NonNull final String showUrl) {
        return imageCache.computeIfAbsent(showUrl, this::fetchImage);
    }

    private String fetchImage(@NonNull final String showUrl) {
        try {
            val img = loader.getSourceByUrl(showUrl).selectFirst(IMAGE_SELECTOR);
            return (img == null)
                ? ""
                : img.absUrl("src").replace(" ", "%20");
        } catch (IOException thrown) {
            log.warn("Не удалось загрузить страницу спектакля {}", showUrl, thrown);
            return "";
        }
    }
}