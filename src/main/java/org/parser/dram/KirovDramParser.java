package org.parser.dram;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.parser.AfishaTextParser;
import org.parser.Parser;
import org.parser.model.Performance;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class KirovDramParser implements Parser<List<Performance>> {

    private static final String ROW_SELECTOR = "#list_afisha > p.h2, #list_afisha > div.t_afisha";
    private static final String MONTH_HEADER_SELECTOR = "p.h2";
    private static final String TITLE_SELECTOR = "h3 a";
    private static final String DATE_SELECTOR = "div.date_afisha";
    private static final String DURATION_SELECTOR = "span.name_hrono:containsOwn(Продолжительность)";
    private static final String AGE_SELECTOR = "div.value_limit";
    private static final String IMAGE_SELECTOR = "img";

    private final AfishaTextParser textParser = new AfishaTextParser();

    @Override
    public List<Performance> parse(final Document document) {
        val result = new ArrayList<Performance>();
        YearMonth currentMonth = null;

        for (Element row : document.select(ROW_SELECTOR)) {
            if (row.is(MONTH_HEADER_SELECTOR)) {
                currentMonth = textParser.parseMonth(row.text());
                continue;
            }
            try {
                val titleElement = row.selectFirst(TITLE_SELECTOR);
                val dateElement = row.selectFirst(DATE_SELECTOR);
                if (titleElement == null || dateElement == null) {
                    log.warn("В карточке нет названия или даты: {}", row.text());
                    continue;
                }
                String title = titleElement.ownText().trim();

                LocalDateTime startsAt = textParser.parseStartsAt(dateElement.text(), currentMonth);
                if (startsAt == null) {
                    log.warn("Не удалось определить дату показа: {}", row.text());
                    continue;
                }

                val ageElement = row.selectFirst(AGE_SELECTOR);
                Integer ageRating = (ageElement == null)
                        ? null
                        : textParser.parseAge(ageElement.text());

                val durationLabel = row.selectFirst(DURATION_SELECTOR);
                Duration duration = (durationLabel == null)
                        ? null
                        : textParser.parseDuration(durationLabel.parent().text());

                val imageLink = row.selectFirst(IMAGE_SELECTOR);
                String imageUrl = (imageLink == null)
                        ? ""
                        : imageLink.absUrl("src");

                result.add(new Performance(title, startsAt, duration, ageRating, imageUrl));
            } catch (Exception thrown) {
                log.warn("Не удалось разобрать карточку афиши: {}", row.text(), thrown);
            }
        }
        return result;
    }
}