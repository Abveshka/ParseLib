package org.parsers.theater.settings;

import org.parsers.common.settings.ParserSettings;

public class KirovKuklaSettings extends ParserSettings {
    public static final String AJAX_URL = "https://kirovkukla.ru/assets/components/ajaxchunk/connector.php";
    public static final String RESOURCE_ID = "239";
    public static final String CHUNK_NAME = "spectacles";

    private static final String BASE_URL = "https://kirovkukla.ru/afisha";

    public KirovKuklaSettings() {
        super(BASE_URL);
    }
}
