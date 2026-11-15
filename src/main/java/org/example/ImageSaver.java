package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Publication;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

@Slf4j
public class ImageSaver implements ParserWorker.OnNewDataHandler<ArrayList<Publication>> {

    private final Path folder;
    private final ImageDownloader downloader;

    public ImageSaver(Path folder, ImageDownloader downloader) {
        this.folder = folder;
        this.downloader = downloader;
    }

    @Override
    public void onNewData(Object sender, ArrayList<Publication> data) {
        for (Publication publication : data) {
            if (isBlank(publication.getImageUrl()) || isBlank(publication.getImageFile())) {
                continue;
            }
            try {
                downloader.download(publication.getImageUrl(),
                        folder.resolve(publication.getImageFile()));
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}