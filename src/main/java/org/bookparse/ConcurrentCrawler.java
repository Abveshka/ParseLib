package org.bookparse;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bookparse.model.Review;
import org.example.HtmlLoader;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public final class ConcurrentCrawler {

    private static final int MAX_ATTEMPTS = 4;
    private static final long FIRST_PAUSE_MS = 1000;

    private final BookmixSettings settings;
    private final HtmlLoader loader;
    private final BookmixListParser listParser = new BookmixListParser();
    private final DiscussionTextExtractor extractor = new DiscussionTextExtractor();

    private final AtomicInteger errors = new AtomicInteger();
    private final AtomicInteger retries = new AtomicInteger();

    public ConcurrentCrawler(final BookmixSettings settings) {
        this.settings = settings;
        this.loader = new HtmlLoader(settings);
    }

    public List<Review> run(final int threads) throws InterruptedException {
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            val start = System.nanoTime();
            val reviews = loadListPages(pool);
            val middle = System.nanoTime();
            loadFullTexts(pool, reviews);
            val end = System.nanoTime();

            log.info("потоков: %d | список: %.1f с | дозагрузка: %.1f с | всего: %.1f с | ошибок: %d | повторов: %d"
                .formatted(threads, (middle - start) / 1e9, (end - middle) / 1e9, (end - start) / 1e9,
                    errors.get(), retries.get()));
            return reviews;
        } finally {
            pool.shutdown();
        }
    }

    private List<Review> loadListPages(final ExecutorService pool) throws InterruptedException {
        val urls = new ArrayList<String>();
        val futures = new ArrayList<Future<List<Review>>>();

        for (int page = settings.getStartPoint(); page <= settings.getEndPoint(); page++) {
            val url = settings.listPageUrl(page);
            urls.add(url);
            futures.add(pool.submit(() -> loadPage(url)));
        }

        val reviews = new ArrayList<Review>();
        for (int i = 0; i < futures.size(); i++) {
            try {
                reviews.addAll(futures.get(i).get());
            } catch (ExecutionException e) {
                errors.incrementAndGet();
                log.error("список | {} | {}", describe(e.getCause()), urls.get(i));
            }
        }
        return reviews;
    }

    private List<Review> loadPage(final String url) throws IOException, InterruptedException {
        return listParser.parse(fetch(url));
    }

    private void loadFullTexts(final ExecutorService pool, final List<Review> reviews)
        throws InterruptedException {
        val toLoad = reviews.stream().filter(Review::isNeedsFullText).toList();
        val futures = new ArrayList<Future<Review>>();

        for (val review : toLoad) {
            futures.add(pool.submit(() -> loadFullText(review)));
        }

        for (int i = 0; i < futures.size(); i++) {
            try {
                futures.get(i).get();
            } catch (ExecutionException e) {
                errors.incrementAndGet();
                log.error("обсуждение | {} | {}", describe(e.getCause()), toLoad.get(i).getDiscussionUrl());
            }
        }
    }

    private Review loadFullText(final Review review) throws IOException, InterruptedException {
        val discussion = fetch(settings.getBaseUrl() + review.getDiscussionUrl());
        val fullText = extractor.extract(discussion, review.getCommentId());
        if (fullText != null) {
            review.setText(fullText);
        }
        return review;
    }

    private Document fetch(final String url) throws IOException, InterruptedException {
        long pause = FIRST_PAUSE_MS;
        for (int attempt = 1; ; attempt++) {
            try {
                return loader.getSourceByUrl(url);
            } catch (IOException e) {
                if (!isRetryable(e) || attempt == MAX_ATTEMPTS) {
                    throw e;
                }
                retries.incrementAndGet();
                log.warn("повтор {}/{} через {} мс | {} | {}",
                    attempt, MAX_ATTEMPTS - 1, pause, describe(e), url);
                Thread.sleep(pause);
                pause *= 2;
            }
        }
    }

    private static boolean isRetryable(final IOException e) {
        if (e instanceof HttpStatusException http) {
            val code = http.getStatusCode();
            return code == 429 || code >= 500;
        }
        return e instanceof SocketTimeoutException || e instanceof SocketException;
    }

    private static String describe(final Throwable t) {
        if (t instanceof HttpStatusException http) {
            val code = http.getStatusCode();
            val name = switch (code) {
                case 403 -> "Forbidden";
                case 404 -> "Not Found";
                case 429 -> "Too Many Requests";
                case 500 -> "Internal Server Error";
                case 502 -> "Bad Gateway";
                case 503 -> "Service Unavailable";
                case 504 -> "Gateway Timeout";
                default -> "HTTP error";
            };
            return code + " - " + name;
        }
        return t.getClass().getSimpleName() + " - " + t.getMessage();
    }
}