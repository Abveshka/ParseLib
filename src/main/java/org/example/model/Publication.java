package org.example.model;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Publication {
    private final String id;
    private final String title;
    private final String author;
    private final Instant publishedAt;
    private final int readingTimeMinutes;
    private final int views;
    private final String imageUrl;
    private final String imageFile;
    private final List<String> hubs;
    private final String preview;

    @Override
    public String toString() {
        return String.format(
                "[%s] %s%n  автор: %s | %s | %d мин | %d просмотров%n  хабы: %s%n  картинка: %s -> %s%n  превью: %s%n",
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