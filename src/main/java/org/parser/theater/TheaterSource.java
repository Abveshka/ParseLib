package org.parser.theater;
import org.parser.model.Theater;
import java.io.IOException;

public interface TheaterSource {
    String getFolderName();
    Theater fetch() throws IOException;
}