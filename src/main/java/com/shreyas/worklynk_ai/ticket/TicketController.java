
package com.shreyas.worklynk_ai.ticket;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    public record CreateTicketRequest(
            @NotNull Long employeeId,
            @NotBlank String subject,
            @NotBlank String description,
            @NotNull Ticket.Category category
    ) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(
            @Valid @RequestBody CreateTicketRequest request) {
        try {
            Ticket ticket = ticketService.createTicket(
                    request.employeeId(),
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

    @GetMapping("/employee/{employeeId}")
    public List<TicketResponse> getEmployeeTickets(
            @PathVariable Long employeeId) {
        return ticketService.getEmployeeTickets(employeeId)
                .stream()
                .map(TicketResponse::from)
                .toList();
    }
    public record UpdateStatusRequest(
        @NotNull Ticket.Status status
) {}
@PatchMapping("/{ticketId}/status")
public TicketResponse updateTicketStatus(
        @PathVariable Long ticketId,
        @Valid @RequestBody UpdateStatusRequest request) {

    try {
        Ticket ticket = ticketService.updateTicketStatus(
                ticketId, request.status());

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
