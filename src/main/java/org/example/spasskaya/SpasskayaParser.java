package org.example.spasskaya;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.example.AfishaTextParser;
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

@Slf4j
public class SpasskayaParser implements Parser<List<Performance>> {

    private static final String ROW_SELECTOR = "table.myafisha tr";
    private static final String MONTH_HEADER_SELECTOR = "h2";
    private static final String TITLE_SELECTOR = "a[href*=/afisha/show/]";
    private static final String DATE_SELECTOR = "a[href*=bilet]";
    private static final String AGE_SELECTOR = "span";
    private static final String DURATION_SELECTOR = "div.page_box div:contains(Продолжительность)";
    private static final String IMAGE_SELECTOR = "img.img_right";

    private final AfishaTextParser textParser = new AfishaTextParser();

    private record ShowDetails(Duration duration, String imageUrl) {}
    private final HtmlLoader loader;
    private final Map<String, ShowDetails> detailsCache = new HashMap<>();

    public SpasskayaParser(HtmlLoader loader) {
        this.loader = loader;
    }

    @Override
    public List<Performance> parse(final Document document) {
        val result = new ArrayList<Performance>();
        YearMonth currentMonth = null;

        for (Element row : document.select(ROW_SELECTOR)) {
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
                String title = titleLink.ownText().trim();

                val ageSpan = titleLink.selectFirst(AGE_SELECTOR);
                Integer ageRating = (ageSpan == null)
                        ? null
                        : textParser.parseAge(ageSpan.text());

                LocalDateTime startsAt = textParser.parseStartsAt(dateLink.text(), currentMonth);
                if (startsAt == null) {
                    log.warn("Не удалось определить дату показа: {}", row.text());
                    continue;
                }

                String showUrl = titleLink.absUrl("href");
                ShowDetails details = loadDetails(showUrl);

                result.add(new Performance(title, startsAt, details.duration(), ageRating, details.imageUrl()));
            } catch (Exception thrown) {
                log.warn("Не удалось разобрать строку афиши: {}", row.text(), thrown);
            }
        }
        return result;
    }

    private ShowDetails fetchDetails(final String showUrl) {
        try {
            Document page = loader.getSourceByUrl(showUrl);

            val durationElement = page.selectFirst(DURATION_SELECTOR);
            Duration duration = (durationElement == null)
                    ? null
                    : textParser.parseDuration(durationElement.text());

            val imageLink = page.selectFirst(IMAGE_SELECTOR);
            String imageUrl = (imageLink == null)
                    ? ""
                    : imageLink.absUrl("src");

            return new ShowDetails(duration, imageUrl);
        } catch (IOException thrown) {
            log.warn("Не удалось загрузить страницу спектакля {}", showUrl, thrown);
            return new ShowDetails(null, "");
        }
    }

    private ShowDetails loadDetails(final String showUrl) {
        return detailsCache.computeIfAbsent(showUrl, this::fetchDetails);
    }
}