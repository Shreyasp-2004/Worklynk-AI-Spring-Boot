package com.shreyas.worklynk_ai.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository
        extends JpaRepository<Ticket, Long> {

    List<Ticket> findByEmployeeId(Long employeeId);

    List<Ticket> findByStatus(Ticket.Status status);

    List<Ticket> findByEmployee_Company_Id(Long companyId);

    List<Ticket> findByEmployee_Company_IdAndStatus(
            Long companyId, Ticket.Status status);
}
