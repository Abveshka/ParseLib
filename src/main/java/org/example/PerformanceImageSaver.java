package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Performance;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class PerformanceImageSaver {

    private final ImageDownloader downloader;

    public PerformanceImageSaver(ImageDownloader downloader) {
        this.downloader = downloader;
    }

    public void save(List<Performance> performances, Path folder) {
        for (Performance performance : performances) {
            String url = performance.getImageUrl();
            if (url == null || url.isBlank()) {
                continue;
            }

            String fileName = fileNameFrom(url);
            if (fileName == null) {
                log.warn("Не удалось определить имя файла по адресу '{}'", url);
                continue;
            }
            Path target = folder.resolve(fileName);

            try {
                downloader.download(url, target);
            } catch (IOException | IllegalArgumentException e) {
                log.warn("Не удалось скачать картинку показа '{}' ({})",
                        performance.getTitle(), url, e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Скачивание картинок прервано");
                return;
            }
        }
    }

    private String fileNameFrom(String url) {
        int queryIndex = url.indexOf('?');
        String cleanUrl = (queryIndex == -1) ? url : url.substring(0, queryIndex);

        int lastSlashIndex = cleanUrl.lastIndexOf('/');
        String fileName = (lastSlashIndex == -1) ? cleanUrl : cleanUrl.substring(lastSlashIndex + 1);

        return fileName.isBlank() ? null : fileName;
    }

}
