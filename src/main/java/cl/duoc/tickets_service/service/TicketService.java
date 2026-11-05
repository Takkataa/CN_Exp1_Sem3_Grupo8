package cl.duoc.tickets_service.service;

import org.springframework.stereotype.Service;

import cl.duoc.tickets_service.dto.TicketRequest;
import cl.duoc.tickets_service.model.Ticket;
import cl.duoc.tickets_service.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service 
@RequiredArgsConstructor 
public class TicketService {

    private final TicketRepository ticketRepository;

    @Value("${efs.path}")
    private String efsPath;

    private Ticket generarTicket(TicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setEventoId(request.getEventoId());
        ticket.setUsuarioId(request.getUsuarioId());
        ticket.setPrecio(request.getPrecio());

        return ticketRepository.save(ticket);
    }
}
