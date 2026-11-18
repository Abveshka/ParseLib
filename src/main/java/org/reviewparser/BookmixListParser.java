package org.reviewparser;

import lombok.val;
import org.reviewparser.model.Review;
import org.parser.Parser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class BookmixListParser implements Parser<List<Review>> {

    private static final Pattern STARS = Pattern.compile("star(\\d)");
    private static final String ELLIPSIS = "...";

    @Override
    public List<Review> parse(final Document document) {
        final List<Review> result = new ArrayList<>();

        for (val block : document.select("div.universal-blocks")) {
            val link = block.selectFirst("div.universal-blocks-title h5 a");
            val book = block.selectFirst("p.universal-blocks-description a[href^=/book.phtml]");
            val user = block.selectFirst("div.universal-blocks-user a.link");
            val rating = block.selectFirst("div.rating");

            val review = new Review();
            review.setBookTitle(book == null ? null : book.text());
            review.setAuthors(block.select("p.universal-blocks-description a[href*=bookauthor.phtml]").eachText());
            review.setReviewAuthor(user == null ? null : user.text());
            review.setStars(parseStars(rating));

            if (link != null) {
                String  href = link.attr("href");
                int hash = href.indexOf('#');
                review.setDiscussionUrl(hash < 0 ? href : href.substring(0, hash));
                review.setCommentId(hash < 0 ? null : href.substring(hash + 1));
            }

            String  snippet = extractSnippet(block);
            review.setText(snippet);
            review.setNeedsFullText(snippet.isEmpty() || snippet.endsWith(ELLIPSIS));

            result.add(review);
        }
        return result;
    }

    private static String extractSnippet(final Element block) {
        val builder = new StringBuilder();
        for (val paragraph : block.select("div.universal-blocks-content > p:not(.universal-blocks-description)")) {
            val copy = paragraph.clone();
            copy.select("a").remove();
            String text = copy.text().trim();
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
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }
}