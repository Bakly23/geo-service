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
import ru.kolpakov.test.geoservice.domain.UserLabel;
import ru.kolpakov.test.geoservice.exceptions.GeoServiceException;
import ru.kolpakov.test.geoservice.storage.GeoStorage;

import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(PlacementCheckController.class)
public class PlacementCheckControllerTest {
    @MockBean
    GeoStorage storageService;
    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setUp() throws InterruptedException {
        Mockito.when(storageService.checkPlacement(argThat(is(new UserLabel(1, 0.1, 0.1)))))
                .thenReturn(true);
        Mockito.when(storageService.checkPlacement(argThat(is(new UserLabel(1, 15.1, 0.1)))))
                .thenReturn(false);
        Mockito.when(storageService.checkPlacement(argThat(is(new UserLabel(3, 15.1, 0.1)))))
                .thenThrow(new GeoServiceException("User with id 3 does not exist"));
    }

    @Test
    public void sunnyDayTest() throws Exception {
        mockMvc.perform(get("/placement?id=1&lon=0.1&lat=0.1"))
                .andExpect(status().is(200))
                .andExpect(content().string("рядом с меткой"));
        mockMvc.perform(get("/placement?id=1&lon=15.1&lat=0.1"))
                .andExpect(status().is(200))
                .andExpect(content().string("вдали от метки"));
    }

    @Test
    public void exceptionTest() throws Exception {
        mockMvc.perform(get("/placement?id=3&lon=15.1&lat=0.1"))
                .andExpect(status().is(400))
                .andExpect(content().string("User with id 3 does not exist"));
    }
}
