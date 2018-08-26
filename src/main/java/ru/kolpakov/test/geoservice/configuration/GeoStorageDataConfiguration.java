package ru.kolpakov.test.geoservice.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.kolpakov.test.geoservice.domain.Coordinates;
import ru.kolpakov.test.geoservice.domain.GeoCellValues;
import ru.kolpakov.test.geoservice.domain.Pair;
import ru.kolpakov.test.geoservice.domain.UserLabel;
import ru.kolpakov.test.geoservice.exceptions.GeoServiceException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class GeoStorageDataConfiguration {
    private final String delimiterString;
    private final ConcurrentMap<Integer, UserLabel> users;
    private final ConcurrentMap<Coordinates, GeoCellValues> geoCells;

    public GeoStorageDataConfiguration(@Value("${delimiter.string:;}") String delimiterString,
                                       @Value("${users.table.path:users.csv}") String usersTablePath,
                                       @Value("${geo.cells.table.path:geo_cells.csv}") String geoCellsTablePath) throws IOException {
        this.delimiterString = delimiterString;
        log.info("Reading geo cells from {}", geoCellsTablePath);
        geoCells = Files.lines(Paths.get(geoCellsTablePath))
                .parallel()
                .map(this::readGeoCellLine)
                .collect(Collectors.toConcurrentMap(Pair::getKey, Pair::getValue));
        log.info("Reading users from {}", geoCellsTablePath);
        users = Files.lines(Paths.get(usersTablePath))
                .parallel()
                .map(this::readUserTableLine)
                .collect(Collectors.toConcurrentMap(UserLabel::getId, Function.identity()));
        log.info("Filling counters for geo cells");
        users.values().parallelStream().forEach(userLabel -> {
            Coordinates cell = new Coordinates(userLabel);
            if (geoCells.containsKey(cell)) {
                geoCells.get(cell).getUserCounter().incrementAndGet();
            } else {
                throw new GeoServiceException(String.format("There is a user %s for which there is no geo cell", userLabel));
            }
        });
    }

    private Pair<Coordinates, GeoCellValues> readGeoCellLine(String line) {
        return readLine("geo cells", line,
                cells -> {
                    double distanceError = Double.parseDouble(cells[2]);
                    if (distanceError < 0) {
                        throw new GeoServiceException("there can be no negative distance error");
                    }
                    return new Pair<>(new Coordinates(Integer.parseInt(cells[0]), Integer.parseInt(cells[1])),
                            new GeoCellValues(distanceError));
                });
    }

    private UserLabel readUserTableLine(String line) {
        return readLine("users", line,
                cells -> new UserLabel(Integer.parseInt(cells[0]), Double.parseDouble(cells[1]), Double.parseDouble(cells[2])));
    }

    private <T> T readLine(String tableName, String line, Function<String[], T> mapper) {
        String[] cells = line.split(delimiterString);
        if (cells.length != 3) {
            throw new GeoServiceException(
                    String.format("%s table files are incorrect, there is a line %s which length is not exactly 3.", tableName, line));
        }
        try {
            return mapper.apply(cells);
        } catch (NumberFormatException e) {
            throw new GeoServiceException(
                    String.format("There is a line %s in %s table files with incorrect number format", line, tableName), e);
        }
    }

    @Bean
    public ConcurrentMap<Integer, UserLabel> getUsers() {
        return users;
    }

    @Bean
    public ConcurrentMap<Coordinates, GeoCellValues> getGeoCells() {
        return geoCells;
    }
}
