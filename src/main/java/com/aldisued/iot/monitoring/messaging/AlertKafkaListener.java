package com.aldisued.iot.monitoring.messaging;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.service.AlertService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AlertKafkaListener {
  private final AlertService alertService;

  public AlertKafkaListener(AlertService alertService) {
    this.alertService = alertService;
  }

  @KafkaListener(topics = KafkaTopics.SENSOR_ALERTS, groupId = "iot-monitoring")
  public void listen(AlertDto alertDto) {
    alertService.saveAlert(alertDto);
  }

}
