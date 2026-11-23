package org.parsers.theater.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jsoup.Jsoup;
import org.parsers.common.html.HtmlLoader;
import org.parsers.theater.TheaterProvider;
import org.parsers.theater.model.Performance;
import org.parsers.theater.model.Theater;
import org.parsers.theater.parser.KirovKuklaParser;
import org.parsers.theater.settings.KirovKuklaSettings;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class KirovKuklaTheaterProvider implements TheaterProvider {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Locale RU = new Locale("ru");
    private static final String SLIDE_SELECTOR = "div.swiper-slide[data-month]:not(.swiper-slide-duplicate)";
    private static final String MONTH_ID_ATTRIBUTE = "data-month";
    private static final Map<String, Month> MONTHS = new HashMap<>();

    static {
        for (val month : Month.values()) {
            MONTHS.put(month.getDisplayName(TextStyle.FULL_STANDALONE, RU).toLowerCase(RU), month);
        }
    }

    @Override
    public String getFolderName() {
        return "kukla";
    }

    @Override
    public Theater fetch() throws IOException {
        val settings = new KirovKuklaSettings();
        val loader = new HtmlLoader(settings);

        val afishaResponse = loader.getResponseByUrl(settings.getBaseUrl());
        val afisha = afishaResponse.parse();
        val cookies = afishaResponse.cookies();

        val parser = new KirovKuklaParser(loader);
        val performances = new ArrayList<Performance>();
        var year = LocalDate.now().getYear();
        var previousMonth = 0;

        for (val slide : afisha.select(SLIDE_SELECTOR)) {
            val month = MONTHS.get(
                slide.text()
                    .trim()
                    .toLowerCase(RU)
            );
            if (month == null) {
                log.warn("Не распознан месяц '{}'", slide.text());
                continue;
            }

            if (month.getValue() < previousMonth) {
                year++;
            }
            previousMonth = month.getValue();

            try {
                val html = loadMonthHtml(loader, settings, slide.attr(MONTH_ID_ATTRIBUTE), cookies);
                val fragment = Jsoup.parse(html, settings.getBaseUrl());
                performances.addAll(parser.parse(fragment, YearMonth.of(year, month)));
            } catch (IOException thrown) {
                log.error("Не удалось загрузить месяц {} {}", month, year, thrown);
            }
        }

        return new Theater("Кировский театр кукол", performances);
    }

    private String loadMonthHtml(
        final HtmlLoader loader,
        final KirovKuklaSettings settings,
        final String monthId,
        final Map<String, String> cookies
    ) throws IOException {
        val form = new LinkedHashMap<String, String>();
        form.put("resource", KirovKuklaSettings.RESOURCE_ID);
        form.put("id_res", monthId);
        form.put("ajaxchunk_name", KirovKuklaSettings.CHUNK_NAME);
        form.put("timestamp", String.valueOf(System.currentTimeMillis()));

        val body = loader.postForm(KirovKuklaSettings.AJAX_URL, settings.getBaseUrl(), form, cookies);

        val json = MAPPER.readTree(body);
        if (!json.path("success").asBoolean()) {
            throw new IOException("Сервер вернул success=false для месяца " + monthId);
        }
        return json.path("content").asText();
    }
}