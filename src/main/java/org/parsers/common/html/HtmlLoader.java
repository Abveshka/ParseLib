package org.parsers.common.html;

import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.parsers.common.settings.ParserSettings;

import java.io.IOException;
import java.util.Map;

@FieldDefaults(makeFinal = true)
public class HtmlLoader {
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final int TIMEOUT_MILLIS = 10_000;

    String url;

    public HtmlLoader(@NonNull final ParserSettings settings) {
        url = settings.getBaseUrl() + "/" + settings.getPrefix();
    }

    public Document getSourceByPageId(final int pageNumber) throws IOException {
        val currentUrl = url.replace("{CurrentId}", Integer.toString(pageNumber));
        return getSourceByUrl(currentUrl);
    }

    public Document getSourceByUrl(@NonNull final String pageUrl) throws IOException {
        return Jsoup.connect(pageUrl)
            .userAgent(USER_AGENT)
            .timeout(TIMEOUT_MILLIS)
            .get();
    }

    public String postForm(
        @NonNull final String postUrl,
        @NonNull final String referer,
        @NonNull final Map<String, String> formData,
        @NonNull final Map<String, String> cookies
    ) throws IOException {
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

    public Connection.Response getResponseByUrl(@NonNull final String pageUrl) throws IOException {
        return Jsoup.connect(pageUrl)
            .userAgent(USER_AGENT)
            .timeout(TIMEOUT_MILLIS)
            .execute();
    }
}