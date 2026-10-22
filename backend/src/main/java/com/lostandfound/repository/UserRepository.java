package com.lostandfound.repository;

import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmailOrPhone(String email, String phone);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByEmailIgnoreCaseOrPhone(String email, String phone);
    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByPhone(String phone);
    List<User> findByStation_StationIdAndRoleIn(UUID stationId, List<Role> roles);
    List<User> findByRole(Role role);

    @Query("select u from User u where (lower(u.name) like lower(concat('%', :q, '%')) "
            + "or lower(coalesce(u.email, '')) like lower(concat('%', :q, '%')) "
            + "or u.phone like concat('%', :q, '%'))")
    Page<User> search(@Param("q") String q, Pageable pageable);

    @Query("select u from User u where u.role = :role and (lower(u.name) like lower(concat('%', :q, '%')) "
            + "or lower(coalesce(u.email, '')) like lower(concat('%', :q, '%')) "
            + "or u.phone like concat('%', :q, '%'))")
    Page<User> searchByRole(@Param("q") String q, @Param("role") Role role, Pageable pageable);
}