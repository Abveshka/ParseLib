package org.example.kukla;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.example.HtmlLoader;
import org.example.model.Performance;
import org.example.model.Theater;
import org.example.theater.TheaterSource;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
public class KirovKuklaTheaterSource implements TheaterSource {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Locale RU = new Locale("ru");
    private static final String SLIDE_SELECTOR =
        "div.swiper-slide[data-month]:not(.swiper-slide-duplicate)";
    private static final String MONTH_ID_ATTRIBUTE = "data-month";

    private static final Map<String, Month> MONTHS = new HashMap<>();
    static {
        for (Month month : Month.values()) {
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

        Connection.Response afishaResponse = loader.getResponseByUrl(settings.getBaseUrl());
        Document afisha = afishaResponse.parse();
        Map<String, String> cookies = afishaResponse.cookies();

        val parser = new KirovKuklaParser(loader);
        val performances = new ArrayList<Performance>();
        int year = LocalDate.now().getYear();
        int previousMonth = 0;

        for (Element slide : afisha.select(SLIDE_SELECTOR)) {
            Month month = MONTHS.get(slide.text().trim().toLowerCase(RU));
            if (month == null) {
                log.warn("Не распознан месяц '{}'", slide.text());
                continue;
            }

            if (month.getValue() < previousMonth) {
                year++;
            }
            previousMonth = month.getValue();

            try {
                String html = loadMonthHtml(loader, settings, slide.attr(MONTH_ID_ATTRIBUTE), cookies);
                Document fragment = Jsoup.parse(html, settings.getBaseUrl());
                performances.addAll(parser.parse(fragment, YearMonth.of(year, month)));
            } catch (IOException thrown) {
                log.error("Не удалось загрузить месяц {} {}", month, year, thrown);
            }
        }

        return new Theater("Кировский театр кукол", performances);
    }

    private String loadMonthHtml(final HtmlLoader loader, final KirovKuklaSettings settings,
                                 final String monthId,
                                 final Map<String, String> cookies) throws IOException {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("resource", KirovKuklaSettings.RESOURCE_ID);
        form.put("id_res", monthId);
        form.put("ajaxchunk_name", KirovKuklaSettings.CHUNK_NAME);
        form.put("timestamp", String.valueOf(System.currentTimeMillis()));

        String body = loader.postForm(KirovKuklaSettings.AJAX_URL, settings.getBaseUrl(), form, cookies);

        JsonNode json = MAPPER.readTree(body);
        if (!json.path("success").asBoolean()) {
            throw new IOException("Сервер вернул success=false для месяца " + monthId);
        }
        return json.path("content").asText();
    }

}