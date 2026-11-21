package org.parsers.theater;

import org.parsers.theater.model.Theater;

import java.io.IOException;

public interface TheaterProvider {
    String getFolderName();

    Theater fetch() throws IOException;
}