package org.example;

import org.example.dram.KirovDramTheaterSource;
import org.example.habr.HabrParser;
import org.example.habr.HabrSettings;
import org.example.kukla.KirovKuklaTheaterSource;
import org.example.model.Publication;
import org.example.theater.TheaterAggregator;
import org.example.theater.TheaterSource;
import org.example.spasskaya.SpasskayaTheaterSource;
import lombok.val;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class NewData implements ParserWorker.OnNewDataHandler<ArrayList<Publication>> {
    @Override
    public void onNewData(Object sender, ArrayList<Publication> data) {
        for (Publication publication : data){
            System.out.println(publication);
        }
    }
}

class Completed implements ParserWorker.OnCompleted {
    @Override
    public void onCompleted(Object sender) {
        System.out.println("Загрузка закончена");
    }
}

public class Main {

    private static final Path OUTPUT_DIR = Path.of("output");
    private static final Path IMAGES_DIR = Path.of("images");

    public static void main(String[] args) throws IOException {
        runHabr();
        runTheaters();
    }

    private static void runHabr() throws IOException {
        ParserWorker<ArrayList<Publication>> worker = new ParserWorker<>(new HabrParser());
        worker.setParserSettings(new HabrSettings(1, 1));

        val jsonSaver = new JsonSaver(OUTPUT_DIR.resolve("habr.json"));
        val imageSaver = new ImageSaver(IMAGES_DIR.resolve("habr"), new ImageDownloader());

        worker.addOnNewDataHandler(new NewData());
        worker.addOnNewDataHandler(imageSaver);
        worker.addOnNewDataHandler(jsonSaver);
        worker.addOnCompletedHandler(jsonSaver);
        worker.addOnCompletedHandler(new Completed());

        worker.start();
    }

    private static void runTheaters() {
        List<TheaterSource> sources = List.of(
                new SpasskayaTheaterSource(),
                new KirovDramTheaterSource(),
                new KirovKuklaTheaterSource());

        val aggregator = new TheaterAggregator(
                sources,
                new TheaterJsonWriter(),
                new PerformanceImageSaver(new ImageDownloader()));

        aggregator.run(OUTPUT_DIR.resolve("afisha.json"), IMAGES_DIR);
    }
}
