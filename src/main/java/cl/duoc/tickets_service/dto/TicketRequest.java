package cl.duoc.tickets_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * DTO (Data Transfer Object) con los datos que el cliente envía
 * para crear un nuevo ticket ({@code POST /api/tickets}).
 *
 * <p>Se usa un DTO en lugar de la entidad {@code Ticket} para que el cliente
 * solo pueda enviar los campos permitidos (no puede fijar el id, las rutas, etc.).
 *
 * <p>Las anotaciones de validación se evalúan cuando el controlador recibe el
 * objeto con {@code @Valid}. Si alguna falla, Spring responde 400 Bad Request.
 */
@Data // Lombok: genera getters, setters, toString, equals y hashCode
public class TicketRequest {

    /** Id del evento. Obligatorio; solo letras, números, guion y guion bajo. */
    @NotBlank(message = "El eventoId es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "El eventoId solo puede tener letras, números, guion y guion bajo")
    private String eventoId;

    /**
     * Id del usuario. Obligatorio; solo letras, números, guion y guion bajo.
     * Restringir los caracteres también evita rutas maliciosas (por ejemplo "../"),
     * ya que eventoId y usuarioId se usan para crear carpetas en el EFS.
     */
    @NotBlank(message = "El usuarioId es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$",
             message = "El usuarioId solo puede tener letras, números, guion y guion bajo")
    private String usuarioId;

    /** Precio del ticket. Obligatorio y mayor a 0. */
    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private Double precio;

}
