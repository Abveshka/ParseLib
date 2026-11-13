package org.example;

import org.jsoup.nodes.Document;

public interface Parser<T> {
    T parse(Document document);
}