package org.parsers;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.parsers.bookmix.BookmixSettings;
import org.parsers.bookmix.ConcurrentCrawler;
import org.parsers.bookmix.output.ReviewJsonWriter;
import org.parsers.common.image.ImageDownloader;
import org.parsers.common.json.JsonFileWriter;
import org.parsers.habr.HabrParser;
import org.parsers.habr.HabrSettings;
import org.parsers.habr.ParserWorker;
import org.parsers.habr.model.Publication;
import org.parsers.habr.output.HabrJsonWriter;
import org.parsers.habr.output.ImageSaver;
import org.parsers.theater.TheaterAggregator;
import org.parsers.theater.output.PerformanceImageSaver;
import org.parsers.theater.provider.KirovDramTheaterProvider;
import org.parsers.theater.provider.KirovKuklaTheaterProvider;
import org.parsers.theater.provider.SpasskayaTheaterProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
class NewData implements ParserWorker.OnNewDataHandler<ArrayList<Publication>> {
    @Override
    public void onNewData(final Object sender, final ArrayList<Publication> data) {
        for (val publication : data) {
            log.info("{}", publication);
        }
    }
}

@Slf4j
class Completed implements ParserWorker.OnCompleted {
    @Override
    public void onCompleted(final Object sender) {
        log.info("Загрузка закончена");
    }
}

@Slf4j
public class Main {
    private static final Path OUTPUT_DIR = Path.of("output");
    private static final Path IMAGES_DIR = Path.of("images");
    private static final int TOTAL_PAGES = 6;
    private static final int CHUNK = 100;
    private static final int THREADS = 8;

    public static void main(String[] args) {
        boolean failed = false;

        try {
            runHabr();
        } catch (IOException thrown) {
            log.error("Не удалось выполнить парсинг Habr", thrown);
            failed = true;
        }

        try {
            runTheaters();
        } catch (IOException thrown) {
            log.error("Не удалось выполнить парсинг афиш", thrown);
            failed = true;
        }

        try {
            runReviews();
        } catch (IOException thrown) {
            log.error("Не удалось выполнить парсинг отзывов", thrown);
            failed = true;
        } catch (InterruptedException thrown) {
            Thread.currentThread().interrupt();
            log.error("Парсинг отзывов прерван", thrown);
            failed = true;
        }

        if (failed) {
            System.exit(1);
        }
    }

    private static void runHabr() throws IOException {
        ParserWorker<ArrayList<Publication>> worker = new ParserWorker<>(new HabrParser());
        worker.setParserSettings(new HabrSettings(1, 1));

        val jsonSaver = new HabrJsonWriter(OUTPUT_DIR.resolve("habr.json"));
        val imageSaver = new ImageSaver(IMAGES_DIR.resolve("habr"), new ImageDownloader());

        worker.addOnNewDataHandler(new NewData());
        worker.addOnNewDataHandler(imageSaver);
        worker.addOnNewDataHandler(jsonSaver);
        worker.addOnCompletedHandler(jsonSaver);
        worker.addOnCompletedHandler(new Completed());

        worker.start();
    }

    private static void runTheaters() throws IOException {
        val sources = List.of(
            new SpasskayaTheaterProvider(),
            new KirovDramTheaterProvider(),
            new KirovKuklaTheaterProvider());

        val aggregator = new TheaterAggregator(
            sources,
            new JsonFileWriter(),
            new PerformanceImageSaver(new ImageDownloader()));

        aggregator.run(OUTPUT_DIR.resolve("afisha.json"), IMAGES_DIR);
    }

    private static void runReviews() throws IOException, InterruptedException {
        val writer = new ReviewJsonWriter(
            OUTPUT_DIR.resolve("reviews.json"),
            new JsonFileWriter());
        val start = System.nanoTime();

        for (int first = 1; first <= TOTAL_PAGES; first += CHUNK) {
            val last = Math.min(first + CHUNK - 1, TOTAL_PAGES);
            val settings = new BookmixSettings(first, last);

            writer.add(new ConcurrentCrawler(settings).run(THREADS));
            log.info("страницы до {} готовы, отзывов: {}", last, writer.size());
        }

        log.info(
            "ГОТОВО: {} отзывов, {} мин", writer.size(),
            "%.1f".formatted((System.nanoTime() - start) / 60e9));
    }
}
