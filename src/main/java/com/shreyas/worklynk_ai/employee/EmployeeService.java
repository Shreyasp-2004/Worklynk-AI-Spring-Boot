package com.shreyas.worklynk_ai.employee;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.shreyas.worklynk_ai.company.CompanyRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Employee createEmployee(
            Long companyId,
            String name,
            String email,
            String rawPassword,
            Employee.Role role) {

        String cleanEmail = email.trim();

        if (employeeRepository.findByEmailIgnoreCase(cleanEmail).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "An employee with this email already exists");
        }

        Employee employee = new Employee();
        employee.setName(name.trim());
        employee.setEmail(cleanEmail);
        employee.setRole(role == null ? Employee.Role.EMPLOYEE : role);
        employee.setPasswordHash(passwordEncoder.encode(rawPassword));
        employee.setCompany(companyRepository.getReferenceById(companyId));

        return employeeRepository.save(employee);
    }

    public List<Employee> listByCompany(Long companyId) {
        return employeeRepository.findByCompanyId(companyId);
    }
}
