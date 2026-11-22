package org.parsers.theater.model;

import lombok.NonNull;

import java.time.Duration;
import java.time.LocalDateTime;

public record Performance(
    @NonNull String title,
    @NonNull LocalDateTime startsAt,
    Duration duration,
    Integer ageRating,
    String imageUrl) {
}