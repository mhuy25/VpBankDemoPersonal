package com.example.vpbankdemopersonal.repository;

import com.example.vpbankdemopersonal.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    Optional<UserRole> findByRoleCode(String roleCode);
}
