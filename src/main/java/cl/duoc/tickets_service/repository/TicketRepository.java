package cl.duoc.tickets_service.repository;

import cl.duoc.tickets_service.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad {@link Ticket}.
 *
 * <p>Al extender {@link JpaRepository} se obtienen, sin escribir código, los métodos
 * CRUD básicos: {@code save}, {@code findById}, {@code findAll}, {@code delete}, etc.
 * El primer tipo genérico es la entidad y el segundo el tipo de su id.
 *
 * <p>Los métodos declarados abajo son "query methods": Spring Data genera la consulta
 * SQL automáticamente a partir del nombre del método.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Busca todos los tickets de un evento.
     * Equivale a: {@code SELECT * FROM tickets WHERE evento_id = ?}
     *
     * @param eventoId identificador del evento
     * @return lista de tickets del evento (vacía si no hay)
     */
    List<Ticket> findByEventoId(String eventoId);

    /**
     * Busca todos los tickets de un usuario.
     * Equivale a: {@code SELECT * FROM tickets WHERE usuario_id = ?}
     *
     * @param usuarioId identificador del usuario
     * @return lista de tickets del usuario (vacía si no hay)
     */
    List<Ticket> findByUsuarioId(String usuarioId);

    /**
     * Busca los tickets de un usuario para un evento específico.
     * Equivale a: {@code SELECT * FROM tickets WHERE evento_id = ? AND usuario_id = ?}
     *
     * @param eventoId  identificador del evento
     * @param usuarioId identificador del usuario
     * @return lista de tickets que cumplen ambas condiciones
     */
    List<Ticket> findByEventoIdAndUsuarioId(String eventoId, String usuarioId);

    // Las consultas de estadísticas se agregan después, en el paso del endpoint de reportes
}
