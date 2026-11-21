package org.parsers.theater.model;

import java.time.Duration;
import java.time.LocalDateTime;

public record Performance(
    String title,
    LocalDateTime startsAt,
    Duration duration,
    Integer ageRating,
    String imageUrl) {
}