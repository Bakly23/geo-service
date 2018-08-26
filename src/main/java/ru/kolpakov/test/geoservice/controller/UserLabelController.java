package ru.kolpakov.test.geoservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kolpakov.test.geoservice.domain.Location;
import ru.kolpakov.test.geoservice.domain.UserLabel;
import ru.kolpakov.test.geoservice.storage.GeoStorage;

@Slf4j
@RestController
@RequestMapping("user_label")
public class UserLabelController {
    private final GeoStorage geoStorage;

    public UserLabelController(GeoStorage geoStorage) {
        this.geoStorage = geoStorage;
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity upsertUser(@PathVariable("id") Integer id, @RequestBody Location location) throws InterruptedException {
        log.info("Requesting upsert of user {} with location {}", id, location);
        return geoStorage.upsertUser(new UserLabel(id, location.getLon(), location.getLat()))
                .map(ul -> new ResponseEntity<>(HttpStatus.NO_CONTENT))
                .orElse(new ResponseEntity<>(HttpStatus.CREATED));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteUser(@PathVariable("id") Integer id) throws InterruptedException {
        log.info("Requesting delete of user with id {}", id);
        return geoStorage.deleteUser(id)
                .map(ul -> new ResponseEntity<>("User Label was deleted", HttpStatus.OK))
                .orElse(new ResponseEntity<>("There was no user label with such id", HttpStatus.OK));
    }
}
