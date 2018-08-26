package ru.kolpakov.test.geoservice.storage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import ru.kolpakov.test.geoservice.domain.Coordinates;
import ru.kolpakov.test.geoservice.domain.GeoCellValues;
import ru.kolpakov.test.geoservice.domain.UserLabel;
import ru.kolpakov.test.geoservice.exceptions.GeoServiceException;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GeoStorage.class, GeoStorageTest.ServiceTestConfiguration.class})
public class GeoStorageTest {
    @Autowired
    GeoStorage storageService;

    @Test
    public void deletePresentUserTest() throws InterruptedException {
        UserLabel prevUserLabel = new UserLabel(1, 0.5, 0.7);
        storageService.upsertUser(prevUserLabel);
        Optional<UserLabel> userLabel = storageService.deleteUser(1);
        assertTrue(userLabel.isPresent());
        assertEquals(prevUserLabel, userLabel.get());
    }

    @Test
    public void deleteAbsentUserTest() throws InterruptedException {
        Optional<UserLabel> userLabel = storageService.deleteUser(5);
        assertFalse(userLabel.isPresent());
    }

    @Test
    public void upsertPresentUserTest() throws InterruptedException {
        UserLabel prevUserLabel = new UserLabel(1, 0.5, 0.7);
        storageService.upsertUser(prevUserLabel);
        Optional<UserLabel> userLabel = storageService.upsertUser(new UserLabel(1, 0.1, 0.3));
        assertTrue(userLabel.isPresent());
        assertEquals(prevUserLabel, userLabel.get());
    }

    @Test
    public void upsertAbsentUserTest() throws InterruptedException {
        storageService.deleteUser(5);
        Optional<UserLabel> userLabel = storageService.upsertUser(new UserLabel(5, 0.1, 0.3));
        assertFalse(userLabel.isPresent());
    }

    @Test(expected = GeoServiceException.class)
    public void upsertUserInNotExistingCellTest() throws InterruptedException {
        storageService.upsertUser(new UserLabel(6, 1.1, 1.3));
    }

    @Test
    public void countInGeoCellTest() throws InterruptedException {
        IntStream.rangeClosed(1, 6).parallel().forEach(i -> {
            try {
                storageService.upsertUser(new UserLabel(i, Math.random(), Math.random()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        assertEquals(6, storageService.countInGeoCell(0.5, 0.5));
    }

    @Test
    public void checkPlacementTest() throws InterruptedException {
        storageService.upsertUser(new UserLabel(1, 0.5, 0.5));
        assertTrue(storageService.checkPlacement(new UserLabel(1, 0.50001, 0.5)));
        assertFalse(storageService.checkPlacement(new UserLabel(1, 0.6, 0.5)));
    }

    @Test(expected = GeoServiceException.class)
    public void checkPlacementNotExistingUserTest() throws InterruptedException {
        storageService.checkPlacement(new UserLabel(7, 0.50001, 0.5));
    }

    @TestConfiguration
    static class ServiceTestConfiguration {
        @Bean
        ConcurrentMap<Coordinates, GeoCellValues> geoCells() {
            ConcurrentMap<Coordinates, GeoCellValues> result = new ConcurrentHashMap<>();
            result.put(new Coordinates(0, 0), new GeoCellValues(10));
            result.put(new Coordinates(1, 0), new GeoCellValues(10));
            result.get(new Coordinates(0, 0)).getUserCounter().set(3);
            result.get(new Coordinates(1, 0)).getUserCounter().set(1);
            return result;
        }

        @Bean
        ConcurrentMap<Integer, UserLabel> users() {
            ConcurrentMap<Integer, UserLabel> result = new ConcurrentHashMap<>();
            result.put(1, new UserLabel(1, 0.5, 0.7));
            result.put(2, new UserLabel(2, 0.1, 0.7));
            result.put(3, new UserLabel(3, 0.6, 0.7));
            result.put(4, new UserLabel(4, 1.5, 0.7));
            return result;
        }

    }
}
