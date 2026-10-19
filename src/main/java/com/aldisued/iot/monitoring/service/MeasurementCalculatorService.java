package com.aldisued.iot.monitoring.service;


import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MeasurementCalculatorService {

  public List<Double> filterByAverageDeviation(List<Double> values, Double deviation) {
    if (deviation == null || deviation < 0.0 || deviation > 1.0) {
      throw new IllegalArgumentException(
          "Deviation must be between 0.0 and 1.0, but was: " + deviation);
    }
    if (values == null) {
      throw new IllegalArgumentException("Values must not be null");
    }
    if (values.isEmpty()) {
      return List.of();
    }

    double average = values.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
    // abs() keeps the range valid for negative averages, e.g. temperatures below zero
    double tolerance = Math.abs(average) * deviation;
    return values.stream()
        .filter(value -> Math.abs(value - average) <= tolerance)
        .toList();
  }

  public List<Double> getMovingAverage(List<Double> data, int windowSize) {
    // TODO: Task 10
    return List.of();
  }

}
