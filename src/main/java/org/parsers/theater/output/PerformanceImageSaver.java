package org.parsers.theater.output;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.parsers.common.image.ImageDownloader;
import org.parsers.theater.model.Performance;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor
public class PerformanceImageSaver {
    ImageDownloader downloader;

    public void save(final List<Performance> performances, final Path folder) {
        for (val performance : performances) {
            val url = performance.imageUrl();
            if (url == null || url.isBlank()) {
                continue;
            }

            val fileName = fileNameFrom(url);
            if (fileName == null) {
                log.warn("Не удалось определить имя файла по адресу '{}'", url);
                continue;
            }
            val target = folder.resolve(fileName);

            try {
                downloader.download(url, target);
            } catch (IOException | IllegalArgumentException thrown) {
                log.warn(
                    "Не удалось скачать картинку показа '{}' ({})",
                    performance.title(), url, thrown);
            } catch (InterruptedException thrown) {
                Thread.currentThread().interrupt();
                log.warn("Скачивание картинок прервано");
                return;
            }
        }
    }

    private String fileNameFrom(final String url) {
        val queryIndex = url.indexOf('?');
        val cleanUrl = (queryIndex == -1)
            ? url
            : url.substring(0, queryIndex);

        val lastSlashIndex = cleanUrl.lastIndexOf('/');
        val fileName = (lastSlashIndex == -1)
            ? cleanUrl
            : cleanUrl.substring(lastSlashIndex + 1);

        return fileName.isBlank() ? null : fileName;
    }
}
