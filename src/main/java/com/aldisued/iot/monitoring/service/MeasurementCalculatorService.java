package com.aldisued.iot.monitoring.service;


import java.util.ArrayList;
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
    if (data == null) {
      throw new IllegalArgumentException("Data must not be null");
    }
    if (windowSize <= 0) {
      throw new IllegalArgumentException("Window size must be positive, but was: " + windowSize);
    }
    if (windowSize > data.size()) {
      throw new IllegalArgumentException(
          "Window size " + windowSize + " exceeds the number of values " + data.size());
    }

    // Running sum: add the value entering the window, subtract the one leaving it
    List<Double> averages = new ArrayList<>(data.size() - windowSize + 1);
    double windowSum = 0.0;
    for (int i = 0; i < data.size(); i++) {
      windowSum += data.get(i);
      if (i >= windowSize) {
        windowSum -= data.get(i - windowSize);
      }
      if (i >= windowSize - 1) {
        averages.add(windowSum / windowSize);
      }
    }
    return averages;
  }

}
