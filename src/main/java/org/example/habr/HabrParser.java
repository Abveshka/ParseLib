package org.example.habr;

import lombok.extern.slf4j.Slf4j;
import org.example.Parser;
import org.example.model.Publication;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class HabrParser implements Parser<ArrayList<Publication>> {

    private static final String CARD_SELECTOR    = "article.tm-articles-list__item";
    private static final String TITLE_SELECTOR   = "a.tm-title__link span";
    private static final String AUTHOR_SELECTOR  = "a.tm-user-info__username";
    private static final String DATE_SELECTOR    = "time[datetime]";
    private static final String READING_SELECTOR = "span.tm-article-reading-time__label";
    private static final String VIEWS_SELECTOR   = "span.tm-icon-counter__value";
    private static final String IMAGE_SELECTOR   = "img.lead-image";
    private static final String HUBS_SELECTOR = "a.tm-publication-hub__link";
    private static final String PREVIEW_SELECTOR = "div.article-formatted-body";

    @Override
    public ArrayList<Publication> parse(Document document) {
        ArrayList<Publication> result = new ArrayList<>();
        for (Element card : document.select(CARD_SELECTOR)) {
            try {
                result.add(parseCard(card));
            } catch (Exception e) {
                log.warn("Не удалось разобрать карточку id={}", card.id(), e);
            }
        }
        log.info("На странице разобрано публикаций: {}", result.size());
        return result;
    }

    private Publication parseCard(Element card) {
        String id = card.id();
        String title = textOf(card, TITLE_SELECTOR);
        if (title.isEmpty()) {
            throw new IllegalStateException("не найдено название");
        }
        String author = textOf(card, AUTHOR_SELECTOR);
        Instant publishedAt = parseDate(id, attrOf(card, DATE_SELECTOR, "datetime"));
        int readingTime = parseNumber(textOf(card, READING_SELECTOR));
        int views = parseViews(textOf(card, VIEWS_SELECTOR));
        String imageUrl = extractImageUrl(card);
        List<String> hubs = parseHubs(card);
        String preview = textOf(card, PREVIEW_SELECTOR);

        String imageFile = buildImageFileName(id, imageUrl);

        return new Publication(id, title, author, publishedAt,
                readingTime, views, imageUrl, imageFile, hubs, preview);
    }

    private String textOf(Element card, String selector) {
        Element e = card.selectFirst(selector);
        return e == null ? "" : e.text();
    }

    private String attrOf(Element card, String selector, String attribute) {
        Element e = card.selectFirst(selector);
        return e == null ? "" : e.attr(attribute);
    }

    private Instant parseDate(String id, String value) {
        if (value.isEmpty()) {
            log.warn("Публикация {}: не найдена дата", id);
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            log.warn("Публикация {}: не удалось разобрать дату '{}'", id, value, e);
            return null;
        }
    }

    private int parseNumber(String text) {
        String digits = text.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    private int parseViews(String text) {
        String s = text.trim().replace(" ", "").replace(",", ".");
        if (s.isEmpty()) {
            return 0;
        }
        double multiplier = 1;
        char last = Character.toUpperCase(s.charAt(s.length() - 1));
        if (last == 'K' || last == 'К') {
            multiplier = 1_000;
            s = s.substring(0, s.length() - 1);
        } else if (last == 'M' || last == 'М') {
            multiplier = 1_000_000;
            s = s.substring(0, s.length() - 1);
        }
        try {
            return (int) Math.round(Double.parseDouble(s) * multiplier);
        } catch (NumberFormatException e) {
            log.warn("Не удалось разобрать число просмотров '{}'", text, e);
            return 0;
        }
    }

    private String extractImageUrl(Element card) {
        Element img = card.selectFirst(IMAGE_SELECTOR);
        if (img == null) {
            log.debug("Публикация {}: нет картинки", card.id());
            return "";
        }
        String url = img.absUrl("src");
        if (url.isEmpty()) {
            url = img.absUrl("data-src");
        }
        return url;
    }

    private List<String> parseHubs(Element card) {
        List<String> hubs = new ArrayList<>();
        for (Element link : card.select(HUBS_SELECTOR)) {
            Element name = link.selectFirst("span");
            if (name != null) {
                hubs.add(name.text());
            }
        }
        return hubs;
    }

    private String buildImageFileName(String id, String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        int queryStart = imageUrl.indexOf('?');
        String path = queryStart >= 0 ? imageUrl.substring(0, queryStart) : imageUrl;
        int dot = path.lastIndexOf('.');
        int slash = path.lastIndexOf('/');
        String extension = dot > slash ? path.substring(dot + 1) : "jpg";
        return id + "." + extension;
    }
}