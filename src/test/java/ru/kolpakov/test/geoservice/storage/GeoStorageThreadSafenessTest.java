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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GeoStorage.class, GeoStorageThreadSafenessTest.ServiceTestConfiguration.class})
public class GeoStorageThreadSafenessTest {
    @Autowired
    GeoStorage storageService;

    @Test
    public void concurrentAddTest() {
        IntStream.range(0, 10).parallel()
                .mapToObj(indexOfConcurrentWrite -> IntStream.rangeClosed(1, 100_000)
                        .mapToObj(i -> new UserLabel(i,
                                indexOfConcurrentWrite + i % 10 + Math.random(),
                                indexOfConcurrentWrite + i % 20 + Math.random())))
                .flatMap(Function.identity())
                .forEach(ul -> {
                    try {
                        storageService.upsertUser(ul);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
        int numberOfUls = IntStream.range(0, 19)
                .boxed()
                .parallel()
                .flatMap(i -> IntStream.range(0, 29).mapToObj(j -> new Coordinates(i, j)))
                .map(c -> storageService.countInGeoCell(c.getX(), c.getY()))
                .reduce(Integer::sum)
                .orElse(-1);
        assertEquals(numberOfUls, 100_000);
        IntStream.rangeClosed(1, 100_000).parallel()
                .forEach(i -> {
                    try {
                        assertTrue(storageService.deleteUser(i).isPresent());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    public void concurrentTest() {
        Set<UserLabel> userLabels = IntStream.rangeClosed(1, 10_000)
                .parallel()
                .mapToObj(i -> new UserLabel(i, i % 10 + Math.random(), i % 20 + Math.random()))
                .collect(Collectors.toSet());
        userLabels.parallelStream().forEach(ul -> {
            try {
                storageService.upsertUser(ul);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        IntStream.range(0, 10)
                .boxed()
                .parallel()
                .flatMap(i -> Stream.of(i, i + 10).map(j -> new Coordinates(i, j)))
                .forEach(c -> assertEquals("wrong for key " + c, 500, storageService.countInGeoCell(c.getX(), c.getY())));
        Set<UserLabel> actualUserLabels = IntStream.rangeClosed(1, 10_000).parallel()
                .mapToObj(i -> {
                    try {
                        return storageService.deleteUser(i).orElseThrow(() -> new IllegalStateException("there must be a user with id " + i));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());
        assertEquals(userLabels, actualUserLabels);
    }


    @TestConfiguration
    static class ServiceTestConfiguration {
        @Bean
        ConcurrentMap<Coordinates, GeoCellValues> geoCells() {
            return IntStream.rangeClosed(0, 19)
                    .boxed()
                    .parallel()
                    .flatMap(i -> IntStream.rangeClosed(0, 29).mapToObj(j -> new Coordinates(i, j)))
                    .collect(Collectors.toConcurrentMap(Function.identity(), c -> new GeoCellValues(10.0)));
        }

        @Bean
        ConcurrentMap<Integer, UserLabel> users() {
            return new ConcurrentHashMap<>();
        }

    }
}
