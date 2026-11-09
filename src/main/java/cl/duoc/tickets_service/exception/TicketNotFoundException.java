package cl.duoc.tickets_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción que se lanza cuando no se encuentra un ticket
 * o el archivo PDF asociado a él.
 *
 * <p>Gracias a {@code @ResponseStatus(HttpStatus.NOT_FOUND)}, si esta excepción
 * llega al controlador sin ser capturada, Spring responde automáticamente
 * con el código HTTP 404 Not Found.
 *
 * <p>Extiende {@link RuntimeException} (excepción no verificada), por lo que
 * no hace falta declararla con {@code throws} en los métodos.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TicketNotFoundException extends RuntimeException{

    /**
     * Crea la excepción con un mensaje estándar a partir del id del ticket.
     *
     * @param id identificador del ticket que no se encontró
     */
    public TicketNotFoundException(Long id){

        super("Ticket no encontrado: " + id);
    }

    /**
     * Crea la excepción con un mensaje personalizado
     * (por ejemplo, cuando el ticket existe pero su PDF no).
     *
     * @param mensaje descripción del error
     */
    public TicketNotFoundException(String mensaje){

        super(mensaje);
    }
}
