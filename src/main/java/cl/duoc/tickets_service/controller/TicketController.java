package cl.duoc.tickets_service.controller;

import cl.duoc.tickets_service.dto.TicketRequest;
import cl.duoc.tickets_service.model.Ticket;
import cl.duoc.tickets_service.service.TicketService;
import cl.duoc.tickets_service.dto.TicketUpdateRequest;

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

/**
 * Controlador REST que expone los endpoints del microservicio de tickets.
 *
 * <p>Su única responsabilidad es recibir las peticiones HTTP, validar la entrada
 * y delegar la lógica de negocio a {@link TicketService}.
 *
 * <p>Resumen de endpoints (todos bajo {@code /api/tickets}):
 * <ul>
 *   <li>{@code POST   /}                       crea un ticket y genera su PDF.</li>
 *   <li>{@code GET    /evento/{eventoId}}      lista los tickets de un evento.</li>
 *   <li>{@code GET    /usuario/{usuarioId}}    lista los tickets de un usuario.</li>
 *   <li>{@code GET    /{id}}                   obtiene un ticket por su id.</li>
 *   <li>{@code POST   /{id}/subir}             sube (o vuelve a subir) el PDF a S3.</li>
 *   <li>{@code GET    /{id}/descargar}         descarga el PDF del ticket desde S3.</li>
 *   <li>{@code PUT    /{id}}                   modifica precio y/o estado.</li>
 *   <li>{@code DELETE /{id}}                   elimina el ticket y su PDF.</li>
 * </ul>
 */
@RestController             // Controlador cuyas respuestas se serializan a JSON
@RequestMapping ("/api/tickets") // Prefijo común para todas las rutas de esta clase
@RequiredArgsConstructor    // Lombok: constructor con los campos final (inyección de dependencias)
public class TicketController {

    /** Servicio con la lógica de negocio; Spring lo inyecta por constructor. */
    private final TicketService ticketService;

    /**
     * Crea un nuevo ticket y genera su PDF.
     *
     * @param request datos del ticket; {@code @Valid} activa las validaciones del DTO
     *                y {@code @RequestBody} indica que vienen en el cuerpo JSON
     * @return 201 Created con el ticket creado
     */
    @PostMapping
    public ResponseEntity<Ticket> generarTicket(@Valid @RequestBody TicketRequest request) {

        Ticket ticket = ticketService.generarTicket(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    /**
     * Lista todos los tickets de un evento.
     *
     * @param eventoId id del evento, tomado de la URL
     * @return 200 OK con la lista (puede venir vacía)
     */
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<Ticket>> listarPorEvento(@PathVariable String eventoId) {
        return ResponseEntity.ok(ticketService.listarPorEvento(eventoId));
    }

    /**
     * Lista todos los tickets de un usuario.
     *
     * @param usuarioId id del usuario, tomado de la URL
     * @return 200 OK con la lista (puede venir vacía)
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Ticket>> listarPorUsuario(@PathVariable String usuarioId) {
        return ResponseEntity.ok(ticketService.listarPorUsuario(usuarioId));
    }

    /**
     * Obtiene un ticket por su id.
     *
     * @param id id del ticket
     * @return 200 OK con el ticket, o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.obtenerPorId(id));
    }

    /**
     * Sube (o vuelve a subir) a S3 el PDF de un ticket que ya está en el EFS.
     *
     * @param id id del ticket
     * @return 200 OK con el ticket y su ruta de S3, o 404 si el ticket o su PDF no existen
     */
    @PostMapping("/{id}/subir")
    public ResponseEntity<Ticket> subirAS3(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.subirTicketAS3(id));
    }

    /**
     * Descarga desde S3 el PDF de un ticket.
     *
     * @param id id del ticket
     * @return 200 OK con el archivo PDF, o 404 si el ticket o su archivo no existen
     */
    @GetMapping("/{id}/descargar")
    public ResponseEntity<Resource> descargar(@PathVariable Long id) {
        Ticket ticket = ticketService.obtenerPorId(id);
        Resource pdf = ticketService.obtenerPdf(id);

        // "attachment" le indica al navegador que descargue el archivo
        // (en vez de mostrarlo) y con qué nombre guardarlo
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(ticket.getNombreArchivo())
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF) // Content-Type: application/pdf
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(pdf);
    }

    /**
     * Modifica el precio y/o el estado de un ticket y regenera su PDF.
     *
     * @param id      id del ticket a modificar
     * @param request campos a cambiar (los que vengan en null no se modifican)
     * @return 200 OK con el ticket actualizado, o 404 si no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<Ticket> modificarTicket(@PathVariable Long id,
                                                  @Valid @RequestBody TicketUpdateRequest request) {
        return ResponseEntity.ok(ticketService.modificarTicket(id, request));
    }

    /**
     * Elimina un ticket y su PDF.
     *
     * @param id id del ticket a eliminar
     * @return 204 No Content si se eliminó, o 404 si no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTicket(@PathVariable Long id) {
        ticketService.eliminarTicket(id);
        return ResponseEntity.noContent().build();
    }
}
