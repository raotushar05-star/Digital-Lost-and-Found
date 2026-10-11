package com.lostandfound.config;

import com.lostandfound.entity.Location;
import com.lostandfound.entity.PoliceStation;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.Role;
import com.lostandfound.repository.LocationRepository;
import com.lostandfound.repository.PoliceStationRepository;
import com.lostandfound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Seeds a default SYSTEM_ADMIN account and demo police accounts tied to the
 * Mangaluru (Barke) station. The station itself is primarily created by
 * Flyway (V2 Bengaluru + V7 Mangaluru); this seeder also creates PS-MGL-001
 * if it is missing so the UI and code stay usable after first boot.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final String DEMO_STATION_CODE = "PS-MGL-001";
    private static final String STATION_ADMIN_EMAIL = "station.admin@lostandfound.local";
    private static final String OFFICER_EMAIL = "officer@lostandfound.local";

    private final UserRepository userRepository;
    private final PoliceStationRepository stationRepository;
    private final LocationRepository locationRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email}")
    private String adminEmail;

    @Value("${app.seed.admin-phone}")
    private String adminPhone;

    @Value("${app.seed.admin-password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = User.builder()
                    .name("System Administrator")
                    .email(adminEmail)
                    .phone(adminPhone)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .role(Role.SYSTEM_ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
        }

        PoliceStation demoStation = ensureMangaluruStation();

        ensurePoliceUser(STATION_ADMIN_EMAIL, "Station Admin", "9999999998", Role.POLICE_ADMIN, demoStation);
        ensurePoliceUser(OFFICER_EMAIL, "Duty Officer", "9999999997", Role.POLICE_OFFICER, demoStation);
    }

    private PoliceStation ensureMangaluruStation() {
        return stationRepository.findByStationCode(DEMO_STATION_CODE).orElseGet(() -> {
            Location location = locationRepository.save(Location.builder()
                    .latitude(new BigDecimal("12.914100"))
                    .longitude(new BigDecimal("74.856000"))
                    .addressText("Barke")
                    .locality("Barke")
                    .city("Mangaluru")
                    .state("Karnataka")
                    .postalCode("575006")
                    .build());
            return stationRepository.save(PoliceStation.builder()
                    .stationName("Barke Police Station")
                    .stationCode(DEMO_STATION_CODE)
                    .address("Barke, Mangaluru, Karnataka")
                    .phone("08242224567")
                    .location(location)
                    .isActive(true)
                    .build());
        });
    }

    private void ensurePoliceUser(String email, String name, String phone, Role role, PoliceStation station) {
        userRepository.findByEmail(email).ifPresentOrElse(existing -> {
            if (existing.getStation() == null
                    || !station.getStationId().equals(existing.getStation().getStationId())) {
                existing.setStation(station);
                userRepository.save(existing);
            }
        }, () -> userRepository.save(User.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode("Police@12345"))
                .role(role)
                .station(station)
                .isActive(true)
                .build()));
    }
}
