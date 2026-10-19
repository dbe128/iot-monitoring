package com.aldisued.iot.monitoring.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration(proxyBeanMethods = false)
public class KafkaConfig {

  @Bean
  @SuppressWarnings("unchecked")
  DefaultKafkaProducerFactoryCustomizer jsonValueSerializer(ObjectMapper objectMapper) {
    return producerFactory -> ((DefaultKafkaProducerFactory<Object, Object>) producerFactory)
        .setValueSerializerSupplier(() -> new JsonSerializer<>(objectMapper).noTypeInfo());
  }

  @Bean
  RecordMessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
    return new JsonMessageConverter(objectMapper);
  }
}
