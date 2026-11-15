package org.example.spasskaya;

import org.example.HtmlLoader;
import org.example.model.Performance;
import org.example.model.Theater;
import org.example.theater.TheaterSource;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

public class SpasskayaTheaterSource implements TheaterSource {

    @Override
    public String getFolderName() {
        return "spasskaya";
    }

    @Override
    public Theater fetch() throws IOException {
        SpasskayaSettings settings = new SpasskayaSettings();
        HtmlLoader loader = new HtmlLoader(settings);

        Document afisha = loader.getSourceByUrl(settings.getBaseUrl());
        List<Performance> performances = new SpasskayaParser(loader).parse(afisha);

        return new Theater("Театр на Спасской", performances);
    }
}