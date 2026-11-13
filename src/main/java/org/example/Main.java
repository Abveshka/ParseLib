package org.example;

import org.example.habr.HabrParser;
import org.example.habr.HabrSettings;
import org.example.model.Publication;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

class NewData implements ParserWorker.OnNewDataHandler<ArrayList<Publication>> {
    @Override
    public void onNewData(Object sender, ArrayList<Publication> data) {
        for (Publication publication : data){
            publication.toString();
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
    public static void main(String[] args) throws IOException {
        ParserWorker<ArrayList<Publication>> parser = new ParserWorker<>(new HabrParser());
        JsonSaver jsonSaver = new JsonSaver(Path.of("output/publications.json"));

        parser.setParserSettings(new HabrSettings(1, 2));
        parser.addOnNewDataHandler(new NewData());
        parser.addOnNewDataHandler(new ImageSaver(Path.of("images")));
        parser.addOnNewDataHandler(jsonSaver);
        parser.addOnCompletedHandler(jsonSaver);

        parser.start();
    }
}
