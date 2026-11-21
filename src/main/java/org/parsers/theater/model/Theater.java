package org.parsers.theater.model;

import java.util.List;

public record Theater(
    String title,
    List<Performance> performances) {
}