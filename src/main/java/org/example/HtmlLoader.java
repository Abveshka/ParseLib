package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class HtmlLoader {

    private static final String USER_AGENT = "Mozilla/5.0";
    private static final int TIMEOUT_MILLIS = 10_000;

    private final String url;

    public HtmlLoader(ParserSettings settings) {
        url = settings.getBaseUrl() + "/" + settings.getPrefix();
    }

    public Document getSourceByPageId(int id) throws IOException {
        String currentUrl = url.replace("{CurrentId}", Integer.toString(id));
        return getSourceByUrl(currentUrl);
    }

    public Document getSourceByUrl(String pageUrl) throws IOException {
        return Jsoup.connect(pageUrl)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MILLIS)
                .get();
    }
}