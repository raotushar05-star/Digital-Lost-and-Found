package com.lostandfound.repository;

import com.lostandfound.entity.PoliceStation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PoliceStationRepository extends JpaRepository<PoliceStation, UUID> {
    List<PoliceStation> findByIsActiveTrue();
    Optional<PoliceStation> findByStationCode(String stationCode);
    boolean existsByStationCode(String stationCode);
}
