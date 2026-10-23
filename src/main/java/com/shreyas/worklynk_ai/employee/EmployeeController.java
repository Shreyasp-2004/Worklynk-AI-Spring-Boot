package com.shreyas.worklynk_ai.employee;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shreyas.worklynk_ai.security.AuthUser;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** HR_ADMIN only (enforced in SecurityConfig). */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public record CreateEmployeeRequest(
            @NotBlank String name,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            Employee.Role role) {
    }

    public record EmployeeResponse(
            Long id, String name, String email, Employee.Role role) {

        static EmployeeResponse from(Employee e) {
            return new EmployeeResponse(
                    e.getId(), e.getName(), e.getEmail(), e.getRole());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody CreateEmployeeRequest request) {

        Employee created = employeeService.createEmployee(
                user.companyId(),
                request.name(),
                request.email(),
                request.password(),
                request.role());

        return EmployeeResponse.from(created);
    }

    @GetMapping
    public List<EmployeeResponse> list(@AuthenticationPrincipal AuthUser user) {
        return employeeService.listByCompany(user.companyId()).stream()
                .map(EmployeeResponse::from)
                .toList();
    }
}
