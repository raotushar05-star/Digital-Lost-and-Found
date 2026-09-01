package com.lostandfound.config;

import com.lostandfound.entity.PoliceStation;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.Role;
import com.lostandfound.repository.PoliceStationRepository;
import com.lostandfound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds a default SYSTEM_ADMIN account (and a demo POLICE_ADMIN tied to the
 * demo station created in V2__seed_reference_data.sql) so the application is
 * usable immediately after first deployment, without requiring direct SQL access.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PoliceStationRepository stationRepository;
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

        PoliceStation demoStation = stationRepository.findByStationCode("PS-BLR-001").orElse(null);
        if (demoStation != null && userRepository.findByEmail("station.admin@lostandfound.local").isEmpty()) {
            User stationAdmin = User.builder()
                    .name("Station Admin")
                    .email("station.admin@lostandfound.local")
                    .phone("9999999998")
                    .passwordHash(passwordEncoder.encode("Police@12345"))
                    .role(Role.POLICE_ADMIN)
                    .station(demoStation)
                    .isActive(true)
                    .build();
            userRepository.save(stationAdmin);
        }
        if (demoStation != null && userRepository.findByEmail("officer@lostandfound.local").isEmpty()) {
            User officer = User.builder()
                    .name("Duty Officer")
                    .email("officer@lostandfound.local")
                    .phone("9999999997")
                    .passwordHash(passwordEncoder.encode("Police@12345"))
                    .role(Role.POLICE_OFFICER)
                    .station(demoStation)
                    .isActive(true)
                    .build();
            userRepository.save(officer);
        }
    }
}
