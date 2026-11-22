package org.parsers.bookmix.output;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.parsers.bookmix.model.Review;
import org.parsers.common.json.JsonFileWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
@Slf4j
public class ReviewJsonWriter {
    @NonNull Path file;
    @NonNull JsonFileWriter writer;
    List<Review> collected = new ArrayList<>();

    public void add(final List<Review> reviews) throws IOException {
        collected.addAll(reviews);
        writer.save(collected, file);
        log.info("Сохранено отзывов в JSON: {} ({})", collected.size(), file);
    }

    public int size() {
        return collected.size();
    }
}