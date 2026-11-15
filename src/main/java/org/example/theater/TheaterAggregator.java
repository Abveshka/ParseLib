package org.example.theater;

import lombok.extern.slf4j.Slf4j;
import org.example.PerformanceImageSaver;
import org.example.TheaterJsonWriter;
import org.example.model.Theater;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TheaterAggregator {

    private final List<TheaterSource> sources;
    private final TheaterJsonWriter jsonWriter;
    private final PerformanceImageSaver imageSaver;

    public TheaterAggregator(List<TheaterSource> sources,
                             TheaterJsonWriter jsonWriter,
                             PerformanceImageSaver imageSaver) {
        this.sources = sources;
        this.jsonWriter = jsonWriter;
        this.imageSaver = imageSaver;
    }

    public void run(Path jsonFile, Path imagesRoot) {
        List<Theater> theaters = new ArrayList<>();

        for (TheaterSource source : sources) {
            try {
                Theater theater = source.fetch();
                theaters.add(theater);
                imageSaver.save(theater.getPerformances(),
                        imagesRoot.resolve(source.getFolderName()));
                log.info("Театр '{}': показов {}", theater.getTitle(),
                        theater.getPerformances().size());
            } catch (IOException e) {
                log.error("Не удалось загрузить афишу театра '{}'", source.getFolderName(), e);
            }
        }

        jsonWriter.save(theaters, jsonFile);
    }
}