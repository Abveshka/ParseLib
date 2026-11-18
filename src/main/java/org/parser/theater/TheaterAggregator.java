package org.parser.theater;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.parser.PerformanceImageSaver;
import org.parser.TheaterJsonWriter;
import org.parser.model.Theater;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TheaterAggregator {

    private final List<TheaterSource> sources;
    private final TheaterJsonWriter jsonWriter;
    private final PerformanceImageSaver imageSaver;

    public TheaterAggregator(final List<TheaterSource> sources,
                             final TheaterJsonWriter jsonWriter,
                             final PerformanceImageSaver imageSaver) {
        this.sources = sources;
        this.jsonWriter = jsonWriter;
        this.imageSaver = imageSaver;
    }

    public void run(final Path jsonFile, final Path imagesRoot) {
        val theaters = new ArrayList<Theater>();

        for (TheaterSource source : sources) {
            try {
                val theater = source.fetch();
                theaters.add(theater);
                imageSaver.save(theater.getPerformances(),
                        imagesRoot.resolve(source.getFolderName()));
                log.info("Театр '{}': показов {}", theater.getTitle(),
                        theater.getPerformances().size());
            } catch (IOException thrown) {
                log.error("Не удалось загрузить афишу театра '{}'", source.getFolderName(), thrown);
            }
        }

        jsonWriter.save(theaters, jsonFile);
    }
}