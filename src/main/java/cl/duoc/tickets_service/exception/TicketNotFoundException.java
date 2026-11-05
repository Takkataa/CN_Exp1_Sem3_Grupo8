package cl.duoc.tickets_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) 
public class TicketNotFoundException extends RuntimeException{

    public TicketNotFoundException(Long id){

        super("Ticket no encontrado: " + id);
    }

    public TicketNotFoundException(String mensaje){

        super(mensaje);
    }
}
