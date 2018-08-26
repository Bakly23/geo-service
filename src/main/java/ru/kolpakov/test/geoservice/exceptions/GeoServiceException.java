package ru.kolpakov.test.geoservice.exceptions;

public class GeoServiceException extends RuntimeException {
    public GeoServiceException(String message) {
        super(message);
    }

    public GeoServiceException(String message, Throwable e) {
        super(message, e);
    }
}
