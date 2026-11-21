package org.parsers.theater.provider;

import org.parsers.common.html.HtmlLoader;
import lombok.val;
import org.parsers.theater.settings.KirovDramSettings;
import org.parsers.theater.model.Theater;
import org.parsers.theater.TheaterProvider;
import org.parsers.theater.parser.KirovDramParser;

import java.io.IOException;

public class KirovDramTheaterProvider implements TheaterProvider {
    @Override
    public String getFolderName() {
        return "dram";
    }

    @Override
    public Theater fetch() throws IOException {
        val settings = new KirovDramSettings();
        val loader = new HtmlLoader(settings);

        val afisha = loader.getSourceByUrl(settings.getBaseUrl());
        val performances = new KirovDramParser().parse(afisha);

        return new Theater("Кировский областной драматический театр", performances);
    }
}
