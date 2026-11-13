package org.example;
import org.example.habr.HabrParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;

public class HtmlLoader {
    private final String url;

    public HtmlLoader(ParserSettings settings) {
        url = settings.baseUrl + "/" + settings.prefix;
    }

    public Document getSourceByPageId(int id) throws IOException {
        String currentUrl = url.replace("{CurrentId}", Integer.toString(id));
        Document doc = Jsoup.connect(currentUrl)
                .userAgent("Mozilla/5.0")
                .get();
        return doc;
    }
}