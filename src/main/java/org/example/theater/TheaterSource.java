package org.example.theater;
import org.example.model.Theater;
import java.io.IOException;

public interface TheaterSource {
    String getFolderName();
    Theater fetch() throws IOException;
}