package ru.kolpakov.test.geoservice.configuration;

import org.junit.Test;
import ru.kolpakov.test.geoservice.exceptions.GeoServiceException;

import java.io.IOException;

public class IncorrectGeoStorageDataConfigurationTest {
    @Test(expected = GeoServiceException.class)
    public void incorrectNumberFormat() throws IOException {
        initializeConfiguration("incorrect_number_format");
    }

    @Test(expected = GeoServiceException.class)
    public void incorrectNumberOfCells() throws IOException {
        initializeConfiguration("incorrect_number_of_cells");
    }

    @Test(expected = GeoServiceException.class)
    public void userWithNoCell() throws IOException {
        initializeConfiguration("user_with_no_cell");
    }

    private void initializeConfiguration(String testName) throws IOException {
        new GeoStorageDataConfiguration(";", "src/test/resources/" + testName + "/users.csv",
                "src/test/resources/" + testName + "/geo_cells.csv"
        );
    }

}
