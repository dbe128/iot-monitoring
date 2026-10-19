package com.aldisued.iot.monitoring.repository;

import com.aldisued.iot.monitoring.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
}
