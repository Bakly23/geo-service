package ru.kolpakov.test.geoservice.domain;

import lombok.Data;

@Data
public class Coordinates {
    private final int x;
    private final int y;

    public Coordinates(UserLabel userLabel) {
        this(userLabel.getLon(), userLabel.getLat());
    }

    public Coordinates(double lon, double lat) {
        this((int) Math.floor(lon), (int) Math.floor(lat));
    }

    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
