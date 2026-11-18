package org.example.kukla;

import org.example.ParserSettings;

public class KirovKuklaSettings extends ParserSettings {

    public static final String AJAX_URL =
        "https://kirovkukla.ru/assets/components/ajaxchunk/connector.php";
    public static final String RESOURCE_ID = "239";
    public static final String CHUNK_NAME = "spectacles";

    @Override
    public String getBaseUrl() {
        return "https://kirovkukla.ru/afisha";
    }
}
