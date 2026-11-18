package org.bookparse.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Review {
    private String bookTitle;
    private List<String> authors;
    private String reviewAuthor;
    private int stars;
    private String text;

    @JsonIgnore
    private String discussionUrl;

    @JsonIgnore
    private String commentId;

    @JsonIgnore
    private boolean needsFullText;
}