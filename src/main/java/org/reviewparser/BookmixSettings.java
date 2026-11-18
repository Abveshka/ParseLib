package org.reviewparser;

import org.parser.ParserSettings;

public final class BookmixSettings extends ParserSettings {

    public static final int PAGE_SIZE = 20;

    private static final String BASE_URL = "https://bookmix.ru";
    private static final String PREFIX = "comments/index.phtml";

    public BookmixSettings(final int startPage, final int endPage) {
        this.baseUrl = BASE_URL;
        this.prefix = PREFIX;
        this.startPoint = startPage;
        this.endPoint = endPage;
    }

    public String listPageUrl(final int page) {
        int begin = (page - 1) * PAGE_SIZE;
        return baseUrl + "/" + prefix
            + "?begin=" + begin
            + "&num_point=" + PAGE_SIZE
            + "&num_points=" + PAGE_SIZE;
    }
}