
package com.shreyas.worklynk_ai.ticket;

import com.shreyas.worklynk_ai.employee.Employee;
import com.shreyas.worklynk_ai.employee.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EmployeeRepository employeeRepository;

    public TicketService(
            TicketRepository ticketRepository,
            EmployeeRepository employeeRepository) {
        this.ticketRepository = ticketRepository;
        this.employeeRepository = employeeRepository;
    }

    public Ticket createTicket(
            Long employeeId,
            String subject,
            String description,
            Ticket.Category category) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found"));

        Ticket ticket = new Ticket();
        ticket.setEmployee(employee);
        ticket.setSubject(subject);
        ticket.setDescription(description);
        ticket.setCategory(category);

        return ticketRepository.save(ticket);
    }

    public List<Ticket> getEmployeeTickets(Long employeeId) {
        return ticketRepository.findByEmployeeId(employeeId);
    }
    public Ticket updateTicketStatus(
        Long ticketId,
        Ticket.Status status) {

    Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() ->
                    new RuntimeException("Ticket not found"));

    ticket.setStatus(status);

    return ticketRepository.save(ticket);
}
}
