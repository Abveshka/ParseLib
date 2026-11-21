package org.parsers.habr.output;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.parsers.common.image.ImageDownloader;
import org.parsers.habr.ParserWorker;
import org.parsers.habr.model.Publication;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

@Slf4j
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor
public class ImageSaver implements ParserWorker.OnNewDataHandler<ArrayList<Publication>> {
    @NonNull Path folder;
    @NonNull ImageDownloader downloader;

    @Override
    public void onNewData(final Object sender, final ArrayList<Publication> data) {
        for (val publication : data) {
            if (publication.imageUrl() == null || publication.imageUrl().isBlank()
                || publication.imageFile() == null || publication.imageFile().isBlank()) {

                continue;
            }
            try {
                downloader.download(
                    publication.imageUrl(),
                    folder.resolve(publication.imageFile()));
            } catch (IOException thrown) {
                log.warn(
                    "Не удалось скачать картинку публикации {} ({})",
                    publication.id(), publication.imageUrl(), thrown);
            } catch (InterruptedException thrown) {
                Thread.currentThread().interrupt();
                log.warn("Скачивание картинки {} прервано", publication.id());
                return;
            }
        }
    }

}