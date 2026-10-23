
package com.shreyas.worklynk_ai.ticket;

import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String subject,
        String description,
        Ticket.Category category,
        Ticket.Status status,
        Ticket.Priority priority,
        Long employeeId,
        LocalDateTime createdAt
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getCategory(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getEmployee().getId(),
                ticket.getCreatedAt()
        );
    }
}
