package org.parser.spasskaya;

import org.parser.HtmlLoader;
import lombok.val;
import org.parser.model.Theater;
import org.parser.theater.TheaterSource;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class SpasskayaTheaterSource implements TheaterSource {

    @Override
    public String getFolderName() {
        return "spasskaya";
    }

    @Override
    public Theater fetch() throws IOException {
        val settings = new SpasskayaSettings();
        val loader = new HtmlLoader(settings);

        Document afisha = loader.getSourceByUrl(settings.getBaseUrl());
        val performances = new SpasskayaParser(loader).parse(afisha);

        return new Theater("Театр на Спасской", performances);
    }
}