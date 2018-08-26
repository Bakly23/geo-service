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

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(UserLabelController.class)
public class UserLabelControllerTest {
    @MockBean
    GeoStorage storageService;
    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setUp() throws InterruptedException {
        UserLabel oldUser = new UserLabel(1, 0.1, 0.1);
        Mockito.when(storageService.upsertUser(argThat(is(oldUser))))
                .thenReturn(Optional.empty());
        Mockito.when(storageService.upsertUser(argThat(is(new UserLabel(1, 10.1, 0.1)))))
                .thenReturn(Optional.of(oldUser));
        Mockito.when(storageService.upsertUser(argThat(is(new UserLabel(2, 11.1, 0.1)))))
                .thenThrow(new GeoServiceException("There is no cell for user"));
        Mockito.when(storageService.deleteUser(1))
                .thenReturn(Optional.of(oldUser));
        Mockito.when(storageService.deleteUser(2))
                .thenReturn(Optional.empty());
    }

    @Test
    public void sunnyDayUpsertTest() throws Exception {
        mockMvc.perform(put("/user_label/1").content("{\"lon\": 0.1, \"lat\": 0.1}").header("Content-Type", "application/json"))
                .andExpect(status().is(201));
        mockMvc.perform(put("/user_label/1").content("{\"lon\": 10.1, \"lat\": 0.1}").header("Content-Type", "application/json"))
                .andExpect(status().is(204));
    }

    @Test
    public void sunnyDayDeleteTest() throws Exception {
        mockMvc.perform(delete("/user_label/1"))
                .andExpect(status().is(200))
                .andExpect(content().string("User Label was deleted"));
        mockMvc.perform(delete("/user_label/2"))
                .andExpect(status().is(200))
                .andExpect(content().string("There was no user label with such id"));
    }

    @Test
    public void exceptionUpsertTest() throws Exception {
        mockMvc.perform(put("/user_label/2").content("{\"lon\": 11.1, \"lat\": 0.1}").header("Content-Type", "application/json"))
                .andExpect(status().is(400))
                .andExpect(content().string("There is no cell for user"));
    }
}
