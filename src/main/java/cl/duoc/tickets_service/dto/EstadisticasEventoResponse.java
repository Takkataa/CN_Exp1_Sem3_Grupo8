package cl.duoc.tickets_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta con las estadísticas de ventas y reservas de un evento
 * ({@code GET /api/tickets/estadisticas} y {@code GET /api/tickets/evento/{eventoId}/estadisticas}).
 *
 * <p>Los contadores y montos parten en 0 y se van sumando según el estado de cada grupo de tickets.
 */
@Data              // Lombok: genera getters, setters, toString, equals y hashCode
@NoArgsConstructor // Lombok: constructor vacío
public class EstadisticasEventoResponse {

    /** Id del evento al que corresponden las estadísticas. */
    private String eventoId;

    /** Cantidad total de tickets del evento (todos los estados). */
    private long totalTickets;

    /** Cantidad de tickets en estado RESERVADO. */
    private long reservados;

    /** Cantidad de tickets en estado VENDIDO. */
    private long vendidos;

    /** Cantidad de tickets en estado CANCELADO. */
    private long cancelados;

    /** Suma de los precios de los tickets VENDIDOS (ingresos del evento). */
    private double montoVendido;

    /** Suma de los precios de los tickets RESERVADOS (ingresos potenciales). */
    private double montoReservado;

    /**
     * Crea las estadísticas de un evento con todos los valores en 0.
     *
     * @param eventoId id del evento
     */
    public EstadisticasEventoResponse(String eventoId) {
        this.eventoId = eventoId;
    }
}
