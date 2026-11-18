package org.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.parser.model.Publication;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;

@Slf4j
public class JsonSaver implements ParserWorker.OnNewDataHandler<ArrayList<Publication>>,
        ParserWorker.OnCompleted {

    private final Path file;
    private final List<Publication> collected = new ArrayList<>();
    private final ObjectMapper mapper;

    public JsonSaver(final Path file) {
        this.file = file;
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void onNewData(final Object sender, final ArrayList<Publication> data) {
        collected.addAll(data);
    }

    @Override
    public void onCompleted(final Object sender) {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            mapper.writeValue(file.toFile(), collected);
            log.info("Сохранено публикаций в JSON: {} ({})", collected.size(), file);
        } catch (IOException thrown) {
            log.error("Не удалось сохранить JSON в {}", file, thrown);
        }
    }
}