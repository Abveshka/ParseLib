package org.example.habr;
import org.example.Parser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class HabrParser implements Parser<ArrayList<String>> {
    @Override
    public ArrayList<String> parse(Document document) {
        ArrayList<String> list = new ArrayList<>();
        Elements elements = document.select("a.tm-title__link span");
        for (Element e: elements) {
            list.add(e.text());
        }
        return list;
    }
}

