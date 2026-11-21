package org.parsers.bookmix;

import lombok.val;
import org.parsers.common.settings.ParserSettings;


public final class BookmixSettings extends ParserSettings {
    public static final int PAGE_SIZE = 20;
    private static final String BASE_URL = "https://bookmix.ru";
    private static final String PREFIX = "comments/index.phtml";

    public BookmixSettings(final int startPage, final int endPage) {
        super(BASE_URL, PREFIX, startPage, endPage);
    }

    public String listPageUrl(final int page) {
        val begin = (page - 1) * PAGE_SIZE;
        return "%s/%s?begin=%d&num_point=%d&num_points=%d"
            .formatted(getBaseUrl(), getPrefix(), begin, PAGE_SIZE, PAGE_SIZE);
    }
}