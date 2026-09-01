package com.lostandfound.repository;

import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmailOrPhone(String email, String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    List<User> findByStation_StationIdAndRoleIn(UUID stationId, List<Role> roles);
    List<User> findByRole(Role role);
}
