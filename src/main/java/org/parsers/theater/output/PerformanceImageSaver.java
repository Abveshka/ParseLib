package org.parsers.theater.output;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.parsers.common.image.ImageDownloader;
import org.parsers.theater.model.Performance;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
@Slf4j
public class PerformanceImageSaver {
    @NonNull ImageDownloader downloader;

    public void save(@NonNull final List<Performance> performances, @NonNull final Path folder) {
        for (val performance : performances) {
            val url = performance.imageUrl();
            if (isBlank(url)) {
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
                log.warn("Не удалось скачать картинку показа '{}' ({})", performance.title(), url, thrown);
            } catch (InterruptedException thrown) {
                Thread.currentThread().interrupt();
                log.warn("Скачивание картинок прервано");
                return;
            }
        }
    }

    private String fileNameFrom(@NonNull final String url) {
        val queryIndex = url.indexOf('?');
        val cleanUrl = (queryIndex == -1)
            ? url
            : url.substring(0, queryIndex);

        val lastSlashIndex = cleanUrl.lastIndexOf('/');
        val fileName = (lastSlashIndex == -1)
            ? cleanUrl
            : cleanUrl.substring(lastSlashIndex + 1);

        return defaultIfBlank(fileName, null);
    }
}
