package com.example.Datadog.alert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    Optional<Alert> findByService_IdAndTypeAndStatus(
            Long serviceId, String type, String status
    );

    List<Alert> findByService_User_EmailOrderByCreatedAtDesc(String email);
}
