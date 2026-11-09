package cl.duoc.tickets_service.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * DTO con los datos que se pueden modificar de un ticket existente
 * ({@code PUT /api/tickets/{id}}).
 *
 * <p>Ambos campos son opcionales: si un campo llega en {@code null},
 * el servicio lo ignora y mantiene el valor actual del ticket.
 * Las validaciones solo se aplican cuando el campo viene informado.
 */
@Data // Lombok: genera getters, setters, toString, equals y hashCode
public class TicketUpdateRequest {

    /** Nuevo precio del ticket. Si se envía, debe ser mayor a 0. */
    @Positive(message = "El precio debe ser mayor a 0")
    private Double precio;

    /** Nuevo estado del ticket. Si se envía, debe ser RESERVADO, VENDIDO o CANCELADO. */
    @Pattern(regexp = "^(RESERVADO|VENDIDO|CANCELADO)$",
             message = "El estado debe ser RESERVADO, VENDIDO o CANCELADO")
    private String estado;

}
