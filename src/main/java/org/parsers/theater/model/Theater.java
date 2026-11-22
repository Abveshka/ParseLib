package org.parsers.theater.model;

import lombok.NonNull;

import java.util.List;

public record Theater(
    @NonNull String title,
    @NonNull List<Performance> performances) {
}