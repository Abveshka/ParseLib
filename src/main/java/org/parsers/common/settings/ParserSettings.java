package org.parsers.common.settings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ParserSettings {
    String baseUrl;
    String prefix;
    int startPoint;
    int endPoint;

    protected ParserSettings(final String baseUrl) {
        this(baseUrl, "", 0, 0);
    }
}