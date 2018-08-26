package ru.kolpakov.test.geoservice.configuration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.kolpakov.test.geoservice.domain.Coordinates;
import ru.kolpakov.test.geoservice.domain.GeoCellValues;
import ru.kolpakov.test.geoservice.domain.UserLabel;

import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"users.table.path=src/test/resources/correct_tables/users.csv",
        "geo.cells.table.path=src/test/resources/correct_tables/geo_cells.csv"})
public class CorrectGeoStorageDataConfigurationTest {
    @Autowired
    ConcurrentMap<Integer, UserLabel> users;
    @Autowired
    ConcurrentMap<Coordinates, GeoCellValues> geoCells;

    @Test
    public void test() {
        assertEquals(3, users.size());
        assertEquals(3, geoCells.size());
        assertEquals(2, geoCells.get(new Coordinates(1, 1)).getUserCounter().intValue());
        assertEquals(1, geoCells.get(new Coordinates(2, 1)).getUserCounter().intValue());
        assertEquals(0, geoCells.get(new Coordinates(3, 1)).getUserCounter().intValue());
        assertEquals(10, geoCells.get(new Coordinates(3, 1)).getDistanceError(), 0.00001);
    }
}
