package org.bookparse;

import lombok.val;
import org.jsoup.nodes.Document;

public class DiscussionTextExtractor {

    public String extract(final Document discussion, final String commentId) {
        val comment = discussion.getElementById(commentId);
        if (comment == null) {
            return null;
        }
        val  content = comment.selectFirst("div.comment-content");
        return content == null
            ? null
            : content.text();
    }
}