package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Publication;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

@Slf4j
public class ImageSaver implements ParserWorker.OnNewDataHandler<ArrayList<Publication>> {

    private final Path folder;
    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public ImageSaver(Path folder) {
        this.folder = folder;
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            log.error("Не удалось создать папку для картинок {}", folder, e);
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void onNewData(Object sender, ArrayList<Publication> data) {
        for (Publication publication : data) {
            if (publication.getImageFile() == null) {
                continue;
            }
            try {
                download(publication);
            } catch (IOException e) {
                log.warn("Не удалось скачать картинку публикации {} ({})",
                        publication.getId(), publication.getImageUrl(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Скачивание картинки {} прервано", publication.getId());
                return;
            }
        }
    }

    private void download(Publication publication) throws IOException, InterruptedException {
        Path target = folder.resolve(publication.getImageFile());
        if (Files.exists(target)) {
            log.debug("Картинка {} уже есть, пропускаю", target);
            return;
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(publication.getImageUrl()))
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