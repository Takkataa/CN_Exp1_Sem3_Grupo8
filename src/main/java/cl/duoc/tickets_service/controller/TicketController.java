package cl.duoc.tickets_service.controller;

import cl.duoc.tickets_service.dto.TicketRequest;
import cl.duoc.tickets_service.model.Ticket;
import cl.duoc.tickets_service.service.TicketService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController 
@RequestMapping ("/api/tickets")
@RequiredArgsConstructor 
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<Ticket> generarTicket(@Valid @RequestBody TicketRequest request) {
        
        Ticket ticket = ticketService.generarTicket(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }
    
}
