package com.shreyas.worklynk_ai.security;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shreyas.worklynk_ai.employee.Employee;
import com.shreyas.worklynk_ai.employee.EmployeeRepository;

@Service
public class AuthService {

    public record LoginResult(
            String token,
            Long employeeId,
            String name,
            String email,
            String role) {
    }

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Empty means "wrong email or password" (deliberately not specific). */
    @Transactional(readOnly = true)
    public Optional<LoginResult> login(String email, String password) {
        Optional<Employee> found =
                employeeRepository.findByEmailIgnoreCase(email.trim());

        if (found.isEmpty()) {
            return Optional.empty();
        }

        Employee employee = found.get();
        String hash = employee.getPasswordHash();

        if (hash == null || !passwordEncoder.matches(password, hash)) {
            return Optional.empty();
        }

        AuthUser user = new AuthUser(
                employee.getId(),
                employee.getEmail(),
                employee.getRole().name(),
                employee.getCompany().getId());

        return Optional.of(new LoginResult(
                jwtService.generateToken(user),
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getRole().name()));
    }
}
