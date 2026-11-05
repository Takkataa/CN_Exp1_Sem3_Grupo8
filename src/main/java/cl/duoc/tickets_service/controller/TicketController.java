package cl.duoc.tickets_service.controller;

import cl.duoc.tickets_service.dto.TicketRequest;
import cl.duoc.tickets_service.model.Ticket;
import cl.duoc.tickets_service.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<Ticket>> listarPorEvento(@PathVariable String eventoId) {
        return ResponseEntity.ok(ticketService.listarPorEvento(eventoId));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Ticket>> listarPorUsuario(@PathVariable String usuarioId) {
        return ResponseEntity.ok(ticketService.listarPorUsuario(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.obtenerPorId(id));
    }

    @GetMapping("/{id}/descargar")
    public ResponseEntity<Resource> descargar(@PathVariable Long id) {
        Ticket ticket = ticketService.obtenerPorId(id);
        Resource pdf = ticketService.obtenerPdf(id);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(ticket.getNombreArchivo())
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(pdf);
    }
}
