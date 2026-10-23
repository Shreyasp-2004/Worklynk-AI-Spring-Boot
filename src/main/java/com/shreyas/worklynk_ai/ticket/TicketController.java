package com.shreyas.worklynk_ai.ticket;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.shreyas.worklynk_ai.security.AuthUser;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // employeeId is gone: the ticket always belongs to the logged-in user.
    public record CreateTicketRequest(
            @NotBlank String subject,
            @NotBlank String description,
            @NotNull Ticket.Category category
    ) {}

    public record UpdateStatusRequest(
            @NotNull Ticket.Status status
    ) {}

    /** Any logged-in user raises a ticket for themselves. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody CreateTicketRequest request) {
        try {
            Ticket ticket = ticketService.createTicket(
                    user.employeeId(),
                    request.subject(),
                    request.description(),
                    request.category()
            );
            return TicketResponse.from(ticket);
        } catch (RuntimeException exception) {
            if ("Employee not found".equals(exception.getMessage())) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found");
            }
            throw exception;
        }
    }

    /** My own tickets. */
    @GetMapping("/me")
    public List<TicketResponse> getMyTickets(
            @AuthenticationPrincipal AuthUser user) {
        return ticketService.getEmployeeTickets(user.employeeId())
                .stream()
                .map(TicketResponse::from)
                .toList();
    }

    /** HR_ADMIN only (SecurityConfig): all tickets in my company. */
    @GetMapping
    public List<TicketResponse> getCompanyTickets(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(required = false) Ticket.Status status) {
        return ticketService.getCompanyTickets(user.companyId(), status)
                .stream()
                .map(TicketResponse::from)
                .toList();
    }

    /** HR_ADMIN only (SecurityConfig). */
    @PatchMapping("/{ticketId}/status")
    public TicketResponse updateTicketStatus(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateStatusRequest request) {
        try {
            Ticket ticket = ticketService.updateTicketStatus(
                    ticketId, request.status(), user.companyId());
            return TicketResponse.from(ticket);
        } catch (RuntimeException exception) {
            if ("Ticket not found".equals(exception.getMessage())) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket not found");
            }
            throw exception;
        }
    }
}
