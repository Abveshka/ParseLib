package org.parsers.theater.settings;

import org.parsers.common.settings.ParserSettings;

public class SpasskayaSettings extends ParserSettings {
    private static final String BASE_URL = "https://ekvus-kirov.ru/afisha";

    public SpasskayaSettings() {
        super(BASE_URL);
    }
}