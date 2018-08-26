package ru.kolpakov.test.geoservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.kolpakov.test.geoservice.domain.UserLabel;
import ru.kolpakov.test.geoservice.storage.GeoStorage;

@Slf4j
@RestController
public class PlacementCheckController {
    private final GeoStorage geoStorage;

    public PlacementCheckController(GeoStorage geoStorage) {
        this.geoStorage = geoStorage;
    }

    @GetMapping("/placement")
    public String isPlacedNearLabel(@RequestParam("id") Integer id,
                                    @RequestParam("lon") double lon,
                                    @RequestParam("lat") double lat) throws InterruptedException {
        log.info("Requesting placement for id {}, longtitude {} and latitude {}", id, lon, lat);
        return geoStorage.checkPlacement(new UserLabel(id, lon, lat)) ? "рядом с меткой" : "вдали от метки";
    }
}
