package org.parsers.habr;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.parsers.common.html.Parser;
import org.parsers.habr.model.Publication;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class HabrParser implements Parser<ArrayList<Publication>> {
    private static final String CARD_SELECTOR = "article.tm-articles-list__item";
    private static final String TITLE_SELECTOR = "a.tm-title__link span";
    private static final String AUTHOR_SELECTOR = "a.tm-user-info__username";
    private static final String DATE_SELECTOR = "time[datetime]";
    private static final String READING_SELECTOR = "span.tm-article-reading-time__label";
    private static final String VIEWS_SELECTOR = "span.tm-icon-counter__value";
    private static final String IMAGE_SELECTOR = "img.lead-image";
    private static final String HUBS_SELECTOR = "a.tm-publication-hub__link";
    private static final String PREVIEW_SELECTOR = "div.article-formatted-body";

    @Override
    public ArrayList<Publication> parse(final Document document) {
        val result = new ArrayList<Publication>();
        for (val card : document.select(CARD_SELECTOR)) {
            try {
                result.add(parseCard(card));
            } catch (Exception thrown) {
                log.warn("Не удалось разобрать карточку id={}", card.id(), thrown);
            }
        }
        log.info("На странице разобрано публикаций: {}", result.size());
        return result;
    }

    private Publication parseCard(final Element card) {
        val id = card.id();
        val title = textOf(card, TITLE_SELECTOR);
        if (title.isEmpty()) {
            throw new IllegalStateException("не найдено название");
        }
        val author = textOf(card, AUTHOR_SELECTOR);
        val publishedAt = parseDate(id, attrOf(card, DATE_SELECTOR, "datetime"));
        if (publishedAt == null) {
            throw new IllegalStateException("не удалось определить дату публикации");
        }
        val readingTime = parseNumber(textOf(card, READING_SELECTOR));
        val views = parseViews(textOf(card, VIEWS_SELECTOR));
        val imageUrl = extractImageUrl(card);
        val hubs = parseHubs(card);
        val preview = textOf(card, PREVIEW_SELECTOR);

        val imageFile = buildImageFileName(id, imageUrl);

        return Publication.builder()
            .id(id)
            .title(title)
            .author(author)
            .publishedAt(publishedAt)
            .readingTimeMinutes(readingTime)
            .views(views)
            .imageUrl(imageUrl)
            .imageFile(imageFile)
            .hubs(hubs)
            .preview(preview)
            .build();
    }

    private String textOf(@NonNull final Element card, @NonNull final String selector) {
        val element = card.selectFirst(selector);
        return element == null
            ? ""
            : element.text();
    }

    private String attrOf(
        @NonNull final Element card,
        @NonNull final String selector,
        @NonNull final String attribute
    ) {
        val element = card.selectFirst(selector);
        return element == null
            ? ""
            : element.attr(attribute);
    }

    private Instant parseDate(@NonNull final String id, @Nullable final String value) {
        if (isBlank(value)) {
            log.warn("Публикация {}: не найдена дата", id);
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException thrown) {
            log.warn("Публикация {}: не удалось разобрать дату '{}'", id, value, thrown);
            return null;
        }
    }

    private int parseNumber(@NonNull final String text) {
        val digits = text.replaceAll("[^0-9]", "");
        return digits.isEmpty()
            ? 0
            : Integer.parseInt(digits);
    }

    private int parseViews(@NonNull final String text) {
        var normalized = text.trim()
            .replace(" ", "")
            .replace(",", ".");
        if (normalized.isEmpty()) {
            return 0;
        }
        var multiplier = 1.0;
        val last = Character.toUpperCase(normalized.charAt(normalized.length() - 1));
        if (last == 'K' || last == 'К') {
            multiplier = 1_000;
            normalized = normalized.substring(0, normalized.length() - 1);
        } else if (last == 'M' || last == 'М') {
            multiplier = 1_000_000;
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        try {
            return (int) Math.round(Double.parseDouble(normalized) * multiplier);
        } catch (NumberFormatException thrown) {
            log.warn("Не удалось разобрать число просмотров '{}'", text, thrown);
            return 0;
        }
    }

    private String extractImageUrl(@NonNull final Element card) {
        val img = card.selectFirst(IMAGE_SELECTOR);
        if (img == null) {
            log.debug("Публикация {}: нет картинки", card.id());
            return "";
        }

        String url = img.absUrl("src");
        return url.isEmpty()
            ? img.absUrl("data-src")
            : url;
    }

    private List<String> parseHubs(@NonNull final Element card) {
        val hubs = new ArrayList<String>();
        for (val link : card.select(HUBS_SELECTOR)) {
            val name = link.selectFirst("span");
            if (name != null) {
                hubs.add(name.text());
            }
        }
        return hubs;
    }

    private String buildImageFileName(@NonNull final String id, @NonNull final String imageUrl) {
        if (isBlank(imageUrl)) {
            return null;
        }
        val queryStart = imageUrl.indexOf('?');
        val path = queryStart >= 0
            ? imageUrl.substring(0, queryStart)
            : imageUrl;
        val dot = path.lastIndexOf('.');
        val slash = path.lastIndexOf('/');
        val extension = dot > slash
            ? path.substring(dot + 1)
            : "jpg";
        return id + "." + extension;
    }
}