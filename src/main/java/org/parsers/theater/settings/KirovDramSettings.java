package org.parsers.theater.settings;

import org.parsers.common.settings.ParserSettings;

public class KirovDramSettings extends ParserSettings {
    private static final String BASE_URL = "https://kirovdramteatr.ru/afisha/";

    public KirovDramSettings() {
        super(BASE_URL);
    }
}
