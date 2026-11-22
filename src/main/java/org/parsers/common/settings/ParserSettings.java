package org.parsers.common.settings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(makeFinal = true)
public abstract class ParserSettings {
    String baseUrl;
    String prefix;
    int startPoint;
    int endPoint;

    protected ParserSettings(final String baseUrl) {
        this(baseUrl, "", 0, 0);
    }
}