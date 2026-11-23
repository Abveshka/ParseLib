package org.parsers.bookmix;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.val;
import org.jsoup.nodes.Document;

public class DiscussionTextExtractor {
    public String extract(@NonNull final Document discussion, @Nullable final String commentId) {
        val comment = discussion.getElementById(commentId);
        if (comment == null) {
            return null;
        }
        val content = comment.selectFirst("div.comment-content");
        return content == null
            ? null
            : content.text();
    }
}