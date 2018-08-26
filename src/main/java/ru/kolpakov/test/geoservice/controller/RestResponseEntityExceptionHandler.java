package ru.kolpakov.test.geoservice.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.kolpakov.test.geoservice.exceptions.GeoServiceException;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = GeoServiceException.class)
    protected ResponseEntity<Object> handleGeoServiceException(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = InterruptedException.class)
    protected ResponseEntity<Object> handleInterruptedException(InterruptedException ex, WebRequest request) {
        return handleExceptionInternal(ex, "Thread that executed this task was interrupted", new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
