package com.aldisued.iot.monitoring.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.exception.AlertNotFoundException;
import com.aldisued.iot.monitoring.service.AlertService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AlertController.class)
class AlertControllerTest {

  private static final UUID SENSOR_ID = UUID.fromString("e3242ea2-0514-46d3-aad8-b2012980c41c");

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AlertService alertService;

  @Test
  void returnsLatestAlert() throws Exception {
    when(alertService.findLastAlertBySensorId(SENSOR_ID)).thenReturn(
        new AlertDto(SENSOR_ID, "Too hot", LocalDateTime.parse("2024-05-01T10:15:30")));

    mockMvc.perform(get("/alerts/latest").queryParam("sensorId", SENSOR_ID.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sensorId").value(SENSOR_ID.toString()))
        .andExpect(jsonPath("$.message").value("Too hot"))
        .andExpect(jsonPath("$.timestamp").value("2024-05-01T10:15:30"));
  }

  @Test
  void returnsNotFoundWhenSensorHasNoAlert() throws Exception {
    when(alertService.findLastAlertBySensorId(SENSOR_ID))
        .thenThrow(new AlertNotFoundException(SENSOR_ID));

    mockMvc.perform(get("/alerts/latest").queryParam("sensorId", SENSOR_ID.toString()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("No alert found for sensor: " + SENSOR_ID));
  }

  @Test
  void rejectsMalformedSensorId() throws Exception {
    mockMvc.perform(get("/alerts/latest").queryParam("sensorId", "not-a-uuid"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(alertService);
  }
}
