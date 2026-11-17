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

    public ImageSaver(final Path folder, final ImageDownloader downloader) {
        this.folder = folder;
        this.downloader = downloader;
    }

    @Override
    public void onNewData(final Object sender, final ArrayList<Publication> data) {
        for (Publication publication : data) {
            if (publication.getImageUrl() == null || publication.getImageUrl().isBlank()
                    || publication.getImageFile() == null || publication.getImageFile().isBlank()) {
                continue;
            }
            try {
                downloader.download(publication.getImageUrl(),
                        folder.resolve(publication.getImageFile()));
            } catch (IOException thrown) {
                log.warn("Не удалось скачать картинку публикации {} ({})",
                        publication.getId(), publication.getImageUrl(), thrown);
            } catch (InterruptedException thrown) {
                Thread.currentThread().interrupt();
                log.warn("Скачивание картинки {} прервано", publication.getId());
                return;
            }
        }
    }

}