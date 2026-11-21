package org.parsers.common.image;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
public class ImageDownloader {
    final HttpClient client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    public void download(final String url, final Path target) throws IOException, InterruptedException {
        Files.createDirectories(target.getParent());
        if (Files.exists(target)) {
            log.debug("Картинка {} уже есть, пропускаю", target);
            return;
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .header("User-Agent", "Mozilla/5.0")
            .build();
        HttpResponse<InputStream> response =
            client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        try (InputStream in = response.body()) {
            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode());
            }
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("Картинка сохранена: {}", target);
    }
}
