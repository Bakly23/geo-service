package ru.kolpakov.test.geoservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.kolpakov.test.geoservice.storage.GeoStorage;

@Slf4j
@RestController
public class CellStatController {
    private final GeoStorage geoStorage;

    public CellStatController(GeoStorage geoStorage) {
        this.geoStorage = geoStorage;
    }

    @GetMapping("/cell_stat")
    public int cellStat(@RequestParam("lon") double lon, @RequestParam("lat") double lat) {
        log.info("Requesting cell stat for longtitude {} and latitude {}", lon, lat);
        return geoStorage.countInGeoCell(lon, lat);
    }
}
