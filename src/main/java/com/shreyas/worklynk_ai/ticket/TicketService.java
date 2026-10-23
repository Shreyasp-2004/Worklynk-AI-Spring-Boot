package com.shreyas.worklynk_ai.ticket;

import com.shreyas.worklynk_ai.employee.Employee;
import com.shreyas.worklynk_ai.employee.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /** All tickets in one company, optionally filtered by status. */
    public List<Ticket> getCompanyTickets(
            Long companyId, Ticket.Status status) {
        return status == null
                ? ticketRepository.findByEmployee_Company_Id(companyId)
                : ticketRepository.findByEmployee_Company_IdAndStatus(
                        companyId, status);
    }

    /**
     * HR can only change tickets from their own company. A ticket from
     * another company is reported as "not found".
     */
    @Transactional
    public Ticket updateTicketStatus(
            Long ticketId,
            Ticket.Status status,
            Long companyId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new RuntimeException("Ticket not found"));

        Long ticketCompanyId =
                ticket.getEmployee().getCompany().getId();

        if (!ticketCompanyId.equals(companyId)) {
            throw new RuntimeException("Ticket not found");
        }

        ticket.setStatus(status);
        return ticketRepository.save(ticket);
    }
}
