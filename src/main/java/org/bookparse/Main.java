package org.bookparse;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bookparse.model.Review;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

@Slf4j
public final class Main {

    private static final int TOTAL_PAGES = 6552;
    private static final int CHUNK = 100;
    private static final int THREADS = 8;
    private static final Path OUTPUT = Path.of("output/reviews.json");

    public static void main(final String[] args) throws Exception {
        collectAll();
    }

    private static void collectAll() throws IOException, InterruptedException {
        val saver = new ReviewJsonSaver();
        val all = new ArrayList<Review>();
        val start = System.nanoTime();

        for (int first = 1; first <= TOTAL_PAGES; first += CHUNK) {
            val last = Math.min(first + CHUNK - 1, TOTAL_PAGES);
            val settings = new BookmixSettings(first, last);
            all.addAll(new ConcurrentCrawler(settings).run(THREADS));

            saver.save(all, OUTPUT);
            log.info("страницы до {} готовы, отзывов: {}", last, all.size());
        }

        log.info("ГОТОВО: {} отзывов, {} мин", all.size(),
            String.format("%.1f", (System.nanoTime() - start) / 60e9));
    }
}