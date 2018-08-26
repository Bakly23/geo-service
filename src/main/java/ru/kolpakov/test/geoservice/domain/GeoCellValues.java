package ru.kolpakov.test.geoservice.domain;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class GeoCellValues {
    private final double distanceError;
    private final AtomicInteger userCounter = new AtomicInteger();
}
