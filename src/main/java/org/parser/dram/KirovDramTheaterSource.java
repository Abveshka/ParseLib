package org.parser.dram;

import org.parser.HtmlLoader;
import lombok.val;
import org.parser.model.Theater;
import org.parser.theater.TheaterSource;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class KirovDramTheaterSource implements TheaterSource {

    @Override
    public String getFolderName() {
        return "dram";
    }

    @Override
    public Theater fetch() throws IOException {
        val settings = new KirovDramSettings();
        val loader = new HtmlLoader(settings);

        Document afisha = loader.getSourceByUrl(settings.getBaseUrl());
        val performances = new KirovDramParser().parse(afisha);

        return new Theater("Кировский областной драматический театр", performances);
    }
}
