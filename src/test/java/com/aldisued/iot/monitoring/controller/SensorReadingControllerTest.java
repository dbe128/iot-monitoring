package com.aldisued.iot.monitoring.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.entity.SensorType;
import com.aldisued.iot.monitoring.exception.SensorNotFoundException;
import com.aldisued.iot.monitoring.service.SensorReadingService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SensorReadingController.class)
class SensorReadingControllerTest {

  private static final UUID SENSOR_ID = UUID.fromString("e3242ea2-0514-46d3-aad8-b2012980c41c");

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private SensorReadingService sensorReadingService;

  @Test
  void returnsSavedReading() throws Exception {
    Sensor sensor = new Sensor("Temperature Sensor", SensorType.TEMPERATURE);
    sensor.setId(SENSOR_ID);
    SensorReading reading = new SensorReading(21.5, LocalDateTime.parse("2024-05-01T10:15:30"),
        sensor);
    reading.setId(42L);
    when(sensorReadingService.saveSensorReading(any())).thenReturn(reading);

    mockMvc.perform(post("/sensor-readings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(readingJson(SENSOR_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(42))
        .andExpect(jsonPath("$.sensorId").value(SENSOR_ID.toString()))
        .andExpect(jsonPath("$.value").value(21.5))
        .andExpect(jsonPath("$.timestamp").value("2024-05-01T10:15:30"));
  }

  @Test
  void returnsNotFoundForUnknownSensor() throws Exception {
    when(sensorReadingService.saveSensorReading(any()))
        .thenThrow(new SensorNotFoundException(SENSOR_ID));

    mockMvc.perform(post("/sensor-readings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(readingJson(SENSOR_ID)))
        .andExpect(status().isNotFound());
  }

  @Test
  void rejectsReadingWithoutValue() throws Exception {
    mockMvc.perform(post("/sensor-readings")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"sensorId": "%s", "timestamp": "2024-05-01T10:15:30"}
                """.formatted(SENSOR_ID)))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(sensorReadingService);
  }

  private static String readingJson(UUID sensorId) {
    return """
        {"sensorId": "%s", "value": 21.5, "timestamp": "2024-05-01T10:15:30"}
        """.formatted(sensorId);
  }
}
