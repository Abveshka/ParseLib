package org.parsers.bookmix;

import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.parsers.bookmix.model.Review;
import org.parsers.common.html.HtmlLoader;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@FieldDefaults(makeFinal = true)
public final class ConcurrentCrawler {
    private static final int MAX_ATTEMPTS = 4;
    private static final long FIRST_PAUSE_MS = 1000;

     BookmixSettings settings;
     HtmlLoader loader;
     BookmixListParser listParser = new BookmixListParser();
     DiscussionTextExtractor extractor = new DiscussionTextExtractor();

     AtomicInteger errors = new AtomicInteger();
     AtomicInteger retries = new AtomicInteger();

    public ConcurrentCrawler(final BookmixSettings settings) {
        this.settings = settings;
        this.loader = new HtmlLoader(settings);
    }

    public List<Review> run(final int threads) throws InterruptedException {
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            val start = System.nanoTime();
            val listed = loadListPages(pool);
            val middle = System.nanoTime();
            val reviews = loadFullTexts(pool, listed);
            val end = System.nanoTime();

            log.info("потоков: %d | список: %.1f с | дозагрузка: %.1f с | всего: %.1f с | ошибок: %d | повторов: %d"
                .formatted(
                    threads, (middle - start) / 1e9, (end - middle) / 1e9, (end - start) / 1e9,
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
        for (int index = 0; index < futures.size(); index++) {
            try {
                reviews.addAll(futures.get(index).get());
            } catch (ExecutionException thrown) {
                errors.incrementAndGet();
                log.error("список | {} | {}", describe(thrown.getCause()), urls.get(index));
            }
        }
        return reviews;
    }

    private List<Review> loadPage(final String url) throws IOException, InterruptedException {
        return listParser.parse(fetch(url));
    }

    private List<Review> loadFullTexts(final ExecutorService pool, final List<Review> reviews)
        throws InterruptedException {
        val futures = new ArrayList<Future<Review>>();

        for (val review : reviews) {
            if (review.needsFullText()) {
                futures.add(pool.submit(() -> loadFullText(review)));
            } else {
                futures.add(CompletableFuture.completedFuture(review));
            }
        }

        val result = new ArrayList<Review>();
        for (int index = 0; index < futures.size(); index++) {
            try {
                result.add(futures.get(index).get());
            } catch (ExecutionException e) {
                val failed = reviews.get(index);
                errors.incrementAndGet();
                log.error("обсуждение | {} | {}", describe(e.getCause()), failed.discussionUrl());
                result.add(failed);
            }
        }
        return result;
    }

    private Review loadFullText(final Review review) throws IOException, InterruptedException {
        val discussion = fetch(settings.getBaseUrl() + review.discussionUrl());
        val fullText = extractor.extract(discussion, review.commentId());
        return fullText == null
            ? review
            : review.withText(fullText).withNeedsFullText(false);
    }

    private Document fetch(final String url) throws IOException, InterruptedException {
        long pause = FIRST_PAUSE_MS;
        for (int attempt = 1; ; attempt++) {
            try {
                return loader.getSourceByUrl(url);
            } catch (IOException thrown) {
                if (!isRetryable(thrown) || attempt == MAX_ATTEMPTS) {
                    throw thrown;
                }
                retries.incrementAndGet();
                log.warn(
                    "повтор {}/{} через {} мс | {} | {}",
                    attempt, MAX_ATTEMPTS - 1, pause, describe(thrown), url);
                Thread.sleep(pause);
                pause *= 2;
            }
        }
    }

    private static boolean isRetryable(final IOException thrown) {
        if (thrown instanceof HttpStatusException http) {
            val code = http.getStatusCode();
            return code == 429 || code >= 500;
        }
        return thrown instanceof SocketTimeoutException || thrown instanceof SocketException;
    }

    private static String describe(final Throwable throwable) {
        if (throwable instanceof HttpStatusException http) {
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
        return throwable.getClass().getSimpleName() + " - " + throwable.getMessage();
    }
}