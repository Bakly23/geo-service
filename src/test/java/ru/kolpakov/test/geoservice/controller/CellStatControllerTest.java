package ru.kolpakov.test.geoservice.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import ru.kolpakov.test.geoservice.exceptions.GeoServiceException;
import ru.kolpakov.test.geoservice.storage.GeoStorage;

import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.doubleThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(CellStatController.class)
public class CellStatControllerTest {
    @MockBean
    GeoStorage storageService;
    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        Mockito.when(storageService.countInGeoCell(doubleThat(is(0.1)), doubleThat(is(0.1))))
                .thenReturn(3);
        Mockito.when(storageService.countInGeoCell(doubleThat(is(1.1)), doubleThat(is(0.1))))
                .thenThrow(new GeoServiceException("There is no such cell"));
    }

    @Test
    public void sunnyDayTest() throws Exception {
        mockMvc.perform(get("/cell_stat?lon=0.1&lat=0.1"))
                .andExpect(status().is(200))
                .andExpect(content().string("3"));
    }

    @Test
    public void exceptionTest() throws Exception {
        mockMvc.perform(get("/cell_stat?lon=1.1&lat=0.1"))
                .andExpect(status().is(400))
                .andExpect(content().string("There is no such cell"));
    }
}
