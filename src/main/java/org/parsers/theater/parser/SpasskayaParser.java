package org.parsers.theater.parser;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jsoup.nodes.Document;
import org.parsers.common.html.HtmlLoader;
import org.parsers.common.html.Parser;
import org.parsers.theater.model.Performance;

import java.io.IOException;
import java.time.Duration;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
@Slf4j
public class SpasskayaParser implements Parser<List<Performance>> {
    private static final String ROW_SELECTOR = "table.myafisha tr";
    private static final String MONTH_HEADER_SELECTOR = "h2";
    private static final String TITLE_SELECTOR = "a[href*=/afisha/show/]";
    private static final String DATE_SELECTOR = "a[href*=bilet]";
    private static final String AGE_SELECTOR = "span";
    private static final String DURATION_SELECTOR = "div.page_box div:contains(Продолжительность)";
    private static final String IMAGE_SELECTOR = "img.img_right";

    AfishaTextParser textParser = new AfishaTextParser();
    @NonNull HtmlLoader loader;
    Map<String, ShowDetails> detailsCache = new HashMap<>();

    private record ShowDetails(
        Duration duration,
        String imageUrl
    ) {
    }


    @Override
    public List<Performance> parse(@NonNull final Document document) {
        val result = new ArrayList<Performance>();
        YearMonth currentMonth = null;

        for (val row : document.select(ROW_SELECTOR)) {
            val header = row.selectFirst(MONTH_HEADER_SELECTOR);
            if (header != null) {
                currentMonth = textParser.parseMonth(header.text());
                continue;
            }
            try {
                val titleLink = row.selectFirst(TITLE_SELECTOR);
                val dateLink = row.selectFirst(DATE_SELECTOR);
                if (titleLink == null || dateLink == null) {
                    continue;
                }
                val title = titleLink.ownText().trim();

                val ageSpan = titleLink.selectFirst(AGE_SELECTOR);
                val ageRating = (ageSpan == null)
                    ? null
                    : textParser.parseAge(ageSpan.text());

                val startsAt = textParser.parseStartsAt(dateLink.text(), currentMonth);
                if (startsAt == null) {
                    log.warn("Не удалось определить дату показа: {}", row.text());
                    continue;
                }

                val showUrl = titleLink.absUrl("href");
                val details = loadDetails(showUrl);

                result.add(new Performance(title, startsAt, details.duration(), ageRating, details.imageUrl()));
            } catch (Exception thrown) {
                log.warn("Не удалось разобрать строку афиши: {}", row.text(), thrown);
            }
        }
        return result;
    }

    private ShowDetails fetchDetails(@NonNull final String showUrl) {
        try {
            val page = loader.getSourceByUrl(showUrl);

            val durationElement = page.selectFirst(DURATION_SELECTOR);
            val duration = (durationElement == null)
                ? null
                : textParser.parseDuration(durationElement.text());

            val imageLink = page.selectFirst(IMAGE_SELECTOR);
            val imageUrl = (imageLink == null)
                ? ""
                : imageLink.absUrl("src");

            return new ShowDetails(duration, imageUrl);
        } catch (IOException thrown) {
            log.warn("Не удалось загрузить страницу спектакля {}", showUrl, thrown);
            return new ShowDetails(null, "");
        }
    }

    private ShowDetails loadDetails(@NonNull final String showUrl) {
        return detailsCache.computeIfAbsent(showUrl, this::fetchDetails);
    }
}