package org.example;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;

public class HtmlLoader {

    private static final String USER_AGENT = "Mozilla/5.0";
    private static final int TIMEOUT_MILLIS = 10_000;

    private final String url;

    public HtmlLoader(final ParserSettings settings) {
        url = settings.getBaseUrl() + "/" + settings.getPrefix();
    }

    public Document getSourceByPageId(final int pageNumber) throws IOException {
        String currentUrl = url.replace("{CurrentId}", Integer.toString(pageNumber));
        return getSourceByUrl(currentUrl);
    }

    public Document getSourceByUrl(final String pageUrl) throws IOException {
        return Jsoup.connect(pageUrl)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MILLIS)
                .get();
    }

    public String postForm(final String postUrl, final String referer,
                           final Map<String, String> formData,
                           final Map<String, String> cookies) throws IOException {
        return Jsoup.connect(postUrl)
            .userAgent(USER_AGENT)
            .referrer(referer)
            .header("X-Requested-With", "XMLHttpRequest")
            .header("Accept", "application/json, text/javascript, */*; q=0.01")
            .cookies(cookies)
            .data(formData)
            .method(Connection.Method.POST)
            .ignoreContentType(true)
            .timeout(TIMEOUT_MILLIS)
            .execute()
            .body();
    }

    public Connection.Response getResponseByUrl(final String pageUrl) throws IOException {
        return Jsoup.connect(pageUrl)
            .userAgent(USER_AGENT)
            .timeout(TIMEOUT_MILLIS)
            .execute();
    }
}