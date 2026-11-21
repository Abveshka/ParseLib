package org.parsers.habr;

import org.parsers.common.settings.ParserSettings;

public class HabrSettings extends ParserSettings {
    private static final String BASE_URL = "https://habr.com/ru/all";
    private static final String PREFIX = "page{CurrentId}";

    public HabrSettings(final int start, final int end) {
        super(BASE_URL, PREFIX, start, end);
    }
}