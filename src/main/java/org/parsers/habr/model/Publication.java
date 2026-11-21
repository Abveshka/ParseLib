package org.parsers.habr.model;

import java.time.Instant;
import java.util.List;

public record Publication(
    String id,
    String title,
    String author,
    Instant publishedAt,
    int readingTimeMinutes,
    int views,
    String imageUrl,
    String imageFile,
    List<String> hubs,
    String preview) {

    @Override
    public String toString() {
        return "[%s] %s%n  автор: %s | %s | %d мин | %d просмотров%n  хабы: %s%n  картинка: %s -> %s%n  превью: %s%n"
            .formatted(
                id, title, author, publishedAt, readingTimeMinutes, views,
                String.join(", ", hubs), imageUrl, imageFile, shortPreview());
    }

    private String shortPreview() {
        if (preview == null || preview.length() <= 100) {
            return preview;
        }
        return preview.substring(0, 100) + "...";
    }
}