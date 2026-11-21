package org.parsers.bookmix.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.With;

import java.util.List;

@With
public record Review(
    String bookTitle,
    List<String> authors,
    String reviewAuthor,
    int stars,
    String text,

    @JsonIgnore
    String discussionUrl,

    @JsonIgnore
    String commentId,

    @JsonIgnore
    boolean needsFullText) {
}