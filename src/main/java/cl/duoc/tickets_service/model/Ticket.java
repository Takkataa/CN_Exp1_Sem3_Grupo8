package cl.duoc.tickets_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un ticket de un evento.
 *
 * <p>Cada instancia corresponde a una fila de la tabla {@code tickets} en la base de datos.
 * Además de los datos del ticket, guarda la ubicación del PDF generado
 * (en EFS y, a futuro, en S3).
 *
 * <p>Anotaciones de Lombok:
 * <ul>
 *   <li>{@code @Data}: genera getters, setters, toString, equals y hashCode.</li>
 *   <li>{@code @NoArgsConstructor}: constructor vacío (obligatorio para JPA).</li>
 *   <li>{@code @AllArgsConstructor}: constructor con todos los atributos.</li>
 * </ul>
 */
@Entity                    // Indica que esta clase se mapea a una tabla
@Table(name = "tickets")   // Nombre de la tabla en la base de datos
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    /** Identificador único, autoincremental, generado por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador del evento al que pertenece el ticket (obligatorio). */
    @Column(nullable = false)
    private String eventoId;

    /** Identificador del usuario dueño del ticket (obligatorio). */
    @Column(nullable = false)
    private String usuarioId;

    /** Nombre del archivo PDF generado, por ejemplo {@code ticket-000001.pdf}. */
    private String nombreArchivo;

    /** Ruta completa del PDF dentro del sistema de archivos EFS. */
    private String rutaEfs;

    /** Ruta/clave del PDF en Amazon S3 (pendiente de implementar). */
    private String rutaS3;

    /** Precio pagado por el ticket. */
    private Double precio;

    /** Estado del ticket: RESERVADO, VENDIDO o CANCELADO. */
    private String estado;

    /** Fecha y hora en que se creó el ticket. Se asigna automáticamente. */
    private LocalDateTime fechaCreacion;

    /**
     * Método que JPA ejecuta automáticamente justo antes de insertar
     * el ticket por primera vez en la base de datos.
     *
     * <p>Asigna la fecha de creación y, si no se indicó un estado,
     * deja el ticket como {@code RESERVADO} por defecto.
     */
    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = "RESERVADO";
        }
    }
}
