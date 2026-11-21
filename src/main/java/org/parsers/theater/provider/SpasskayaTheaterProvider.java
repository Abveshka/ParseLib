package org.parsers.theater.provider;

import lombok.val;
import org.parsers.common.html.HtmlLoader;
import org.parsers.theater.TheaterProvider;
import org.parsers.theater.model.Theater;
import org.parsers.theater.parser.SpasskayaParser;
import org.parsers.theater.settings.SpasskayaSettings;

import java.io.IOException;

public class SpasskayaTheaterProvider implements TheaterProvider {
    @Override
    public String getFolderName() {
        return "spasskaya";
    }

    @Override
    public Theater fetch() throws IOException {
        val settings = new SpasskayaSettings();
        val loader = new HtmlLoader(settings);

        val afisha = loader.getSourceByUrl(settings.getBaseUrl());
        val performances = new SpasskayaParser(loader).parse(afisha);

        return new Theater("Театр на Спасской", performances);
    }
}