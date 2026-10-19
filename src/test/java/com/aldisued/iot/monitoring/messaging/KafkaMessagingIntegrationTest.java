package com.aldisued.iot.monitoring.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.aldisued.iot.monitoring.IntegrationTestBase;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorType;
import com.aldisued.iot.monitoring.repository.AlertRepository;
import com.aldisued.iot.monitoring.repository.SensorReadingRepository;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaConnectionDetails;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

class KafkaMessagingIntegrationTest extends IntegrationTestBase {

  private static final Duration TIMEOUT = Duration.ofSeconds(30);
  private static final String TIMESTAMP = "2024-05-01T10:15:30";

  @TestConfiguration(proxyBeanMethods = false)
  static class KafkaContainerConfiguration {

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
      return new KafkaContainer(DockerImageName.parse("apache/kafka:3.9.1"));
    }
  }

  @Autowired
  private KafkaConnectionDetails kafkaConnectionDetails;

  @Autowired
  private SensorRepository sensorRepository;

  @Autowired
  private SensorReadingRepository sensorReadingRepository;

  @Autowired
  private AlertRepository alertRepository;

  private Sensor sensor;

  @BeforeEach
  void setup() {
    sensor = sensorRepository.save(new Sensor("Kafka Sensor", SensorType.TEMPERATURE));
  }

  @AfterEach
  void cleanup() {
    sensorReadingRepository.deleteAll();
    alertRepository.deleteAll();
    sensorRepository.deleteAll();
  }

  @Test
  void persistsSensorReadingReceivedFromKafka() throws Exception {
    send(KafkaTopics.SENSOR_READINGS, """
        {"sensorId": "%s", "value": 21.5, "timestamp": "%s"}
        """.formatted(sensor.getId(), TIMESTAMP));

    await().atMost(TIMEOUT).untilAsserted(() ->
        assertThat(sensorReadingRepository.findAll()).singleElement().satisfies(reading -> {
          assertThat(reading.getValue()).isEqualTo(21.5);
          assertThat(reading.getTimestamp()).isEqualTo(LocalDateTime.parse(TIMESTAMP));
        }));
  }

  @Test
  void persistsAlertReceivedFromKafkaAndPublishesIt() throws Exception {
    try (Consumer<String, String> alertsConsumer = alertsConsumer()) {
      send(KafkaTopics.SENSOR_ALERTS, """
          {"sensorId": "%s", "message": "Too hot", "timestamp": "%s"}
          """.formatted(sensor.getId(), TIMESTAMP));

      await().atMost(TIMEOUT).untilAsserted(() ->
          assertThat(alertRepository.findAll()).singleElement().satisfies(alert -> {
            assertThat(alert.getMessage()).isEqualTo("Too hot");
            assertThat(alert.getTimestamp()).isEqualTo(LocalDateTime.parse(TIMESTAMP));
          }));

      List<String> published = new ArrayList<>();
      await().atMost(TIMEOUT).untilAsserted(() -> {
        alertsConsumer.poll(Duration.ofMillis(200)).forEach(record -> published.add(record.value()));
        assertThat(published).containsExactly(
            "{\"sensorId\":\"%s\",\"message\":\"Too hot\",\"timestamp\":\"%s\"}"
                .formatted(sensor.getId(), TIMESTAMP));
      });
    }
  }

  private void send(String topic, String payload) throws Exception {
    try (Producer<String, String> producer = new KafkaProducer<>(
        Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers()),
        new StringSerializer(), new StringSerializer())) {
      producer.send(new ProducerRecord<>(topic, payload)).get();
    }
  }

  private Consumer<String, String> alertsConsumer() {
    Consumer<String, String> consumer = new KafkaConsumer<>(
        Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers(),
            ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"),
        new StringDeserializer(), new StringDeserializer());
    consumer.subscribe(List.of(KafkaTopics.ALERTS));
    return consumer;
  }

  private Object bootstrapServers() {
    return String.join(",", kafkaConnectionDetails.getBootstrapServers());
  }
}
