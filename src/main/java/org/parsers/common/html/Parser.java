package org.parsers.common.html;

import org.jsoup.nodes.Document;

public interface Parser<T> {
    T parse(final Document document);
}