package cl.duoc.tickets_service.repository;

import cl.duoc.tickets_service.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByEventoId(String eventoId);

    List<Ticket> findByUsuarioId(String usuarioId);

    List<Ticket> findByEventoIdAndUsuarioId(String eventoId, String usuarioId);

    // Las consultas de estadísticas se agregan después, en el paso del endpoint de reportes
}