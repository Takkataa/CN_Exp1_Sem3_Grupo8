package cl.duoc.tickets_service.repository;

import cl.duoc.tickets_service.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad {@link Ticket}.
 *
 * <p>Al extender {@link JpaRepository} se obtienen, sin escribir código, los métodos
 * CRUD básicos: {@code save}, {@code findById}, {@code findAll}, {@code delete}, etc.
 * El primer tipo genérico es la entidad y el segundo el tipo de su id.
 *
 * <p>Los métodos {@code findBy...} son "query methods": Spring Data genera la consulta
 * SQL automáticamente a partir del nombre del método. Los métodos con {@code @Query}
 * usan una consulta JPQL escrita a mano (JPQL usa nombres de clases y atributos, no de tablas).
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

    /**
     * Historial de tickets generados con filtros opcionales y combinables.
     *
     * <p>Cada condición tiene la forma {@code (:param IS NULL OR ...)}: si el parámetro
     * llega en {@code null}, la condición se cumple siempre y ese filtro no se aplica.
     * Los resultados vienen del más reciente al más antiguo.
     *
     * @param eventoId  filtra por evento (opcional)
     * @param usuarioId filtra por usuario (opcional)
     * @param estado    filtra por estado (opcional)
     * @param desde     incluye tickets creados desde esta fecha y hora (opcional)
     * @param hasta     incluye tickets creados antes de esta fecha y hora (opcional, exclusivo)
     * @return lista de tickets que cumplen todos los filtros indicados
     */
    @Query("""
            SELECT t FROM Ticket t
            WHERE (:eventoId IS NULL OR t.eventoId = :eventoId)
              AND (:usuarioId IS NULL OR t.usuarioId = :usuarioId)
              AND (:estado IS NULL OR t.estado = :estado)
              AND (:desde IS NULL OR t.fechaCreacion >= :desde)
              AND (:hasta IS NULL OR t.fechaCreacion < :hasta)
            ORDER BY t.fechaCreacion DESC
            """)
    List<Ticket> buscarHistorial(@Param("eventoId") String eventoId,
                                 @Param("usuarioId") String usuarioId,
                                 @Param("estado") String estado,
                                 @Param("desde") LocalDateTime desde,
                                 @Param("hasta") LocalDateTime hasta);

    /**
     * Resumen de un evento agrupado por estado.
     * Cada fila es un arreglo: {@code [estado (String), cantidad (Long), sumaPrecios (Double)]}.
     *
     * @param eventoId identificador del evento
     * @return una fila por cada estado que tenga tickets en el evento
     */
    @Query("""
            SELECT t.estado, COUNT(t), COALESCE(SUM(t.precio), 0)
            FROM Ticket t
            WHERE t.eventoId = :eventoId
            GROUP BY t.estado
            """)
    List<Object[]> resumenPorEstado(@Param("eventoId") String eventoId);

    /**
     * Resumen de todos los eventos agrupado por evento y estado.
     * Cada fila es un arreglo: {@code [eventoId (String), estado (String), cantidad (Long), sumaPrecios (Double)]}.
     *
     * @return una fila por cada combinación evento/estado que tenga tickets
     */
    @Query("""
            SELECT t.eventoId, t.estado, COUNT(t), COALESCE(SUM(t.precio), 0)
            FROM Ticket t
            GROUP BY t.eventoId, t.estado
            ORDER BY t.eventoId
            """)
    List<Object[]> resumenPorEventoYEstado();
}
