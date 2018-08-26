package ru.kolpakov.test.geoservice.domain;

import lombok.Data;

@Data
public class UserLabel {
    private final Integer id;
    private final double lon;
    private final double lat;
}
