package org.parser.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Theater {
    private final String title;
    private final List<Performance> performances;
}
