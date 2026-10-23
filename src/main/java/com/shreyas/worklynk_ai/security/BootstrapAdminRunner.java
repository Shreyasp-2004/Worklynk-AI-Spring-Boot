package com.shreyas.worklynk_ai.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.shreyas.worklynk_ai.company.Company;
import com.shreyas.worklynk_ai.company.CompanyRepository;
import com.shreyas.worklynk_ai.employee.Employee;
import com.shreyas.worklynk_ai.employee.EmployeeRepository;

/**
 * Creates the first HR admin so you can log in at all. Runs only when no
 * HR_ADMIN exists yet. If an employee with the configured email already
 * exists (for example from earlier Postman testing), that employee is
 * promoted to HR_ADMIN and given the configured password.
 */
@Component
public class BootstrapAdminRunner implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(BootstrapAdminRunner.class);

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    private final String companyName;
    private final String adminName;
    private final String adminEmail;
    private final String adminPassword;

    public BootstrapAdminRunner(
            EmployeeRepository employeeRepository,
            CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder,
            @Value("${worklynk.bootstrap.company-name:Demo Company}")
            String companyName,
            @Value("${worklynk.bootstrap.admin-name:HR Admin}")
            String adminName,
            @Value("${worklynk.bootstrap.admin-email:admin@worklynk.local}")
            String adminEmail,
            @Value("${worklynk.bootstrap.admin-password:}")
            String adminPassword) {
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyName = companyName;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (employeeRepository.existsByRole(Employee.Role.HR_ADMIN)) {
            return;
        }

        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("No HR_ADMIN exists and worklynk.bootstrap.admin-password "
                    + "is not set, so nobody will be able to log in.");
            return;
        }

        Company company = companyRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    Company created = new Company();
                    created.setName(companyName);
                    return companyRepository.save(created);
                });

        Employee admin = employeeRepository
                .findByEmailIgnoreCase(adminEmail)
                .orElseGet(Employee::new);

        if (admin.getName() == null) {
            admin.setName(adminName);
        }
        if (admin.getEmail() == null) {
            admin.setEmail(adminEmail);
        }
        if (admin.getCompany() == null) {
            admin.setCompany(company);
        }
        admin.setRole(Employee.Role.HR_ADMIN);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));

        employeeRepository.save(admin);
        log.info("HR admin ready: {}", admin.getEmail());
    }
}
