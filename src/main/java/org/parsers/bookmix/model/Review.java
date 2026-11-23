package org.parsers.bookmix.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.NonNull;
import lombok.With;

import java.util.List;

@Builder
@With
public record Review(
    @NonNull String bookTitle,
    @NonNull List<String> authors,
    @NonNull String reviewAuthor,
    int stars,
    @NonNull String text,
    @JsonIgnore String discussionUrl,
    @JsonIgnore String commentId,
    @JsonIgnore boolean needsFullText
) {
}