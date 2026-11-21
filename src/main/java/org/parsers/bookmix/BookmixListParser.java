package org.parsers.bookmix;

import lombok.val;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.parsers.bookmix.model.Review;
import org.parsers.common.html.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class BookmixListParser implements Parser<List<Review>> {
    private static final Pattern STARS = Pattern.compile("star(\\d)");
    private static final String ELLIPSIS = "...";

    @Override
    public List<Review> parse(final Document document) {
        val result = new ArrayList<Review>();

        for (val block : document.select("div.universal-blocks")) {
            val link = block.selectFirst("div.universal-blocks-title h5 a");
            val book = block.selectFirst("p.universal-blocks-description a[href^=/book.phtml]");
            val user = block.selectFirst("div.universal-blocks-user a.link");
            val rating = block.selectFirst("div.rating");

            val href = link == null
                ? null
                : link.attr("href");
            val hash = href == null
                ? -1
                : href.indexOf('#');
            val snippet = extractSnippet(block);

            result.add(Review.builder()
                .bookTitle(book == null
                    ? null
                    : book.text())
                .authors(block.select("p.universal-blocks-description a[href*=bookauthor.phtml]").eachText())
                .reviewAuthor(user == null
                    ? null
                    : user.text())
                .stars(parseStars(rating))
                .text(snippet)
                .discussionUrl(hash < 0
                    ? href
                    : href.substring(0, hash))
                .commentId(hash < 0
                    ? null
                    : href.substring(hash + 1))
                .needsFullText(snippet.isEmpty() || snippet.endsWith(ELLIPSIS))
                .build());
        }
        return result;
    }

    private static String extractSnippet(final Element block) {
        val builder = new StringBuilder();
        for (val paragraph : block.select("div.universal-blocks-content > p:not(.universal-blocks-description)")) {
            val copy = paragraph.clone();
            copy.select("a").remove();
            val text = copy.text().trim();
            if (!text.isEmpty()) {
                if (!builder.isEmpty()) {
                    builder.append(' ');
                }
                builder.append(text);
            }
        }
        return builder.toString();
    }

    private static int parseStars(final Element rating) {
        if (rating == null) {
            return 0;
        }
        val matcher = STARS.matcher(rating.className());
        return matcher.find()
            ? Integer.parseInt(matcher.group(1))
            : 0;
    }
}