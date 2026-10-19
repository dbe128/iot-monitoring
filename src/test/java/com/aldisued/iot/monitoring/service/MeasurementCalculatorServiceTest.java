package com.aldisued.iot.monitoring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class MeasurementCalculatorServiceTest {

  private final MeasurementCalculatorService calculator = new MeasurementCalculatorService();

  @Test
  void keepsValuesOnTheBoundaryOfTheAcceptedRange() {
    assertThat(calculator.filterByAverageDeviation(List.of(5.0, 10.0, 15.0), 0.5))
        .containsExactly(5.0, 10.0, 15.0);
  }

  @Test
  void keepsOnlyValuesEqualToAverageWithZeroDeviation() {
    assertThat(calculator.filterByAverageDeviation(List.of(5.0, 10.0, 15.0), 0.0))
        .containsExactly(10.0);
  }

  @Test
  void filtersAroundNegativeAverage() {
    assertThat(calculator.filterByAverageDeviation(List.of(-10.0, -12.0, -20.0), 0.2))
        .containsExactly(-12.0);
  }

  @Test
  void keepsOriginalOrderOfValues() {
    assertThat(calculator.filterByAverageDeviation(List.of(12.0, 30.0, 10.0, 11.0, 7.0), 0.3))
        .containsExactly(12.0, 10.0, 11.0);
  }

  @Test
  void rejectsMissingDeviation() {
    assertThatThrownBy(() -> calculator.filterByAverageDeviation(List.of(1.0), null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsMissingValues() {
    assertThatThrownBy(() -> calculator.filterByAverageDeviation(null, 0.5))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
