package com.shreyas.worklynk_ai.employee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByEmailIgnoreCase(String email);

    boolean existsByRole(Employee.Role role);

    List<Employee> findByCompanyId(Long companyId);
}
