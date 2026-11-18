package org.parser.model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Performance {
    private final String title;
    private final LocalDateTime startsAt;
    private final Duration duration;
    private final Integer ageRating;
    private final String imageUrl;
}
