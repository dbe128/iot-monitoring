package com.aldisued.iot.monitoring.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorType;
import com.aldisued.iot.monitoring.exception.SensorAlreadyExistsException;
import com.aldisued.iot.monitoring.service.SensorService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SensorController.class)
class SensorControllerTest {

  private static final String VALID_SENSOR = """
      {"name": "Temperature Sensor", "type": "TEMPERATURE"}
      """;

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private SensorService sensorService;

  @Test
  void returnsCreatedSensor() throws Exception {
    UUID id = UUID.randomUUID();
    Sensor sensor = new Sensor("Temperature Sensor", SensorType.TEMPERATURE);
    sensor.setId(id);
    when(sensorService.saveSensor(any())).thenReturn(sensor);

    mockMvc.perform(post("/sensors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALID_SENSOR))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.name").value("Temperature Sensor"))
        .andExpect(jsonPath("$.type").value("TEMPERATURE"));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "{\"type\": \"TEMPERATURE\"}",
      "{\"name\": \"  \", \"type\": \"TEMPERATURE\"}",
      "{\"name\": \"Temperature Sensor\"}"
  })
  void rejectsInvalidSensor(String body) throws Exception {
    mockMvc.perform(post("/sensors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(sensorService);
  }

  @Test
  void returnsConflictForDuplicateName() throws Exception {
    when(sensorService.saveSensor(any()))
        .thenThrow(new SensorAlreadyExistsException("Temperature Sensor"));

    mockMvc.perform(post("/sensors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALID_SENSOR))
        .andExpect(status().isConflict());
  }

  @Test
  void returnsConflictWhenUniqueConstraintIsViolated() throws Exception {
    when(sensorService.saveSensor(any()))
        .thenThrow(new DataIntegrityViolationException("duplicate key"));

    mockMvc.perform(post("/sensors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALID_SENSOR))
        .andExpect(status().isConflict());
  }
}
