package org.example.dram;

import org.example.HtmlLoader;
import org.example.model.Performance;
import org.example.model.Theater;
import org.example.theater.TheaterSource;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

public class KirovDramTheaterSource implements TheaterSource {

    @Override
    public String getFolderName() {
        return "dram";
    }

    @Override
    public Theater fetch() throws IOException {
        KirovDramSettings settings = new KirovDramSettings();
        HtmlLoader loader = new HtmlLoader(settings);

        Document afisha = loader.getSourceByUrl(settings.getBaseUrl());
        List<Performance> performances = new KirovDramParser().parse(afisha);

        return new Theater("Кировский областной драматический театр", performances);
    }
}
