package org.parsers.theater;

import lombok.RequiredArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.parsers.common.json.JsonFileWriter;
import org.parsers.theater.model.Theater;
import org.parsers.theater.output.PerformanceImageSaver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor
public class TheaterAggregator {
    @NonNull List<TheaterProvider> sources;
    @NonNull JsonFileWriter jsonWriter;
    @NonNull PerformanceImageSaver imageSaver;

    public void run(@NonNull final Path jsonFile, @NonNull final Path imagesRoot) throws IOException {
        val theaters = new ArrayList<Theater>();

        for (val source : sources) {
            try {
                val theater = source.fetch();
                theaters.add(theater);
                imageSaver.save(
                    theater.performances(),
                    imagesRoot.resolve(source.getFolderName()));
                log.info(
                    "Театр '{}': показов {}", theater.title(),
                    theater.performances().size());
            } catch (IOException thrown) {
                log.error("Не удалось загрузить афишу театра '{}'", source.getFolderName(), thrown);
            }
        }

        jsonWriter.save(theaters, jsonFile);
    }
}