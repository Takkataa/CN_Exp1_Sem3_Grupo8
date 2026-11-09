package cl.duoc.tickets_service.service;

import cl.duoc.tickets_service.dto.TicketRequest;
import cl.duoc.tickets_service.model.Ticket;
import cl.duoc.tickets_service.repository.TicketRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import cl.duoc.tickets_service.dto.TicketUpdateRequest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import cl.duoc.tickets_service.exception.TicketNotFoundException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import java.util.List;

/**
 * Servicio con la lógica de negocio de los tickets.
 *
 * <p>Se encarga de:
 * <ul>
 *   <li>Crear, consultar, modificar y eliminar tickets en la base de datos.</li>
 *   <li>Generar el PDF de cada ticket (con la librería OpenPDF) y guardarlo
 *       en el EFS con la estructura {@code {efsPath}/{eventoId}/{usuarioId}/ticket-XXXXXX.pdf}.</li>
 *   <li>Subir, actualizar, descargar y eliminar ese PDF en Amazon S3
 *       con la misma estructura {@code {eventoId}/{usuarioId}/ticket-XXXXXX.pdf}.</li>
 * </ul>
 *
 * <p>El EFS funciona como almacenamiento temporal (donde se genera el archivo)
 * y S3 como almacenamiento definitivo (desde donde se descarga).
 */
@Service                 // Registra la clase como bean de servicio en Spring
@RequiredArgsConstructor // Lombok: constructor con los campos final (inyección de dependencias)
public class TicketService {

    /** Repositorio para acceder a la tabla de tickets. */
    private final TicketRepository ticketRepository;

    /** Servicio para las operaciones con Amazon S3. */
    private final S3Service s3Service;

    /** Carpeta base donde se guardan los PDF. Se lee de {@code efs.path} en application.properties. */
    @Value("${efs.path}")
    private String efsPath;

    /**
     * Crea un ticket, genera su PDF en el EFS, lo sube a S3 y guarda ambas rutas en la base de datos.
     *
     * <p>El ticket se guarda primero para que la base de datos le asigne un id
     * (necesario para nombrar el PDF). Luego se guarda la ruta del EFS antes de subir
     * a S3: así, si la subida falla, el PDF queda registrado y se puede reintentar
     * con {@code POST /api/tickets/{id}/subir}.
     *
     * @param request datos validados del ticket
     * @return el ticket guardado, con su id, fecha, estado y rutas del PDF
     * @throws RuntimeException si no se puede generar el PDF o subirlo a S3
     */
    public Ticket generarTicket(TicketRequest request) {
        //se crea para tener su id
        Ticket ticket = new Ticket();
        ticket.setEventoId(request.getEventoId());
        ticket.setUsuarioId(request.getUsuarioId());
        ticket.setPrecio(request.getPrecio());
        ticket = ticketRepository.save(ticket);

        try {
            //Se nombra el archivo y la carpeta
            // %06d rellena el id con ceros a la izquierda hasta 6 dígitos (ej. 42 -> ticket-000042.pdf)
            String nombreArchivo = String.format("ticket-%06d.pdf", ticket.getId());
            Path carpeta = Paths.get(efsPath, request.getEventoId(), request.getUsuarioId());

            //Crea la carpeta si no existe
            Files.createDirectories(carpeta);

            //Se genera el pdf
            Path rutaFinal = carpeta.resolve(nombreArchivo);
            generarPdf(ticket, rutaFinal);

            //Se actualiza el ticket en la base de datos con la ruta del EFS
            ticket.setNombreArchivo(nombreArchivo);
            ticket.setRutaEfs(rutaFinal.toString());
            ticket = ticketRepository.save(ticket);

        } catch (IOException | DocumentException e) {
            // Se envuelve en una RuntimeException para no obligar al controlador a manejarla
            throw new RuntimeException("No se pudo generar el PDF del ticket", e);
        }

        // Se sube el PDF a S3 y se registra la clave del objeto
        subirPdfAS3(ticket);
        return ticketRepository.save(ticket);
    }

    /**
     * Sube a S3 el PDF de un ticket que ya existe en el EFS.
     * Sirve para cumplir el endpoint "subir tickets a S3" y para reintentar
     * una subida que haya fallado al crear el ticket.
     *
     * @param id id del ticket
     * @return el ticket con su ruta de S3 actualizada
     * @throws TicketNotFoundException si el ticket no existe o su PDF no está en el EFS
     */
    public Ticket subirTicketAS3(Long id) {
        Ticket ticket = obtenerPorId(id);

        // Sin PDF en el EFS no hay nada que subir
        if (ticket.getRutaEfs() == null || !Files.exists(Paths.get(ticket.getRutaEfs()))) {
            throw new TicketNotFoundException("El ticket " + id + " no tiene un PDF en el EFS");
        }

        subirPdfAS3(ticket);
        return ticketRepository.save(ticket);
    }

    /**
     * Sube el PDF del EFS a S3 con la clave {@code eventoId/usuarioId/nombreArchivo}
     * y deja esa clave en el ticket (sin guardarlo en la base de datos).
     * Si el objeto ya existe en S3, se reemplaza.
     *
     * @param ticket ticket con {@code rutaEfs} y {@code nombreArchivo} ya asignados
     */
    private void subirPdfAS3(Ticket ticket) {
        String key = s3Service.construirKey(ticket.getEventoId(), ticket.getUsuarioId(), ticket.getNombreArchivo());
        s3Service.subirArchivo(key, Paths.get(ticket.getRutaEfs()));
        ticket.setRutaS3(key);
    }

    /**
     * Genera el archivo PDF de un ticket en la ruta indicada.
     * Si el archivo ya existe, se sobrescribe.
     *
     * @param ticket      ticket cuyos datos se escriben en el PDF
     * @param rutaArchivo ruta completa del archivo a crear
     * @throws IOException       si no se puede escribir el archivo
     * @throws DocumentException si ocurre un error al construir el PDF
     */
    private void generarPdf(Ticket ticket, Path rutaArchivo) throws IOException, DocumentException{

        Document document = new Document();
        // try-with-resources: el FileOutputStream se cierra automáticamente al terminar
        try (FileOutputStream fos = new FileOutputStream(rutaArchivo.toFile())){

            // Asocia el documento con el archivo de salida y lo abre para escribir
            PdfWriter.getInstance(document, fos);
            document.open();

            // Fuentes del título y del cuerpo
            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD,20);
            Font texto = FontFactory.getFont(FontFactory.HELVETICA, 12);

            // Encabezado centrado con espacio debajo
            Paragraph encabezado = new Paragraph("TICKET DEL EVENTO", titulo);
            encabezado.setAlignment(Element.ALIGN_CENTER);
            encabezado.setSpacingAfter(20);
            document.add(encabezado);

            // Datos del ticket, una línea por campo
            document.add(new Paragraph("Ticket N°: " + ticket.getId(), texto));
            document.add(new Paragraph("Evento: " + ticket.getEventoId(), texto));
            document.add(new Paragraph("Usuario: " + ticket.getUsuarioId(), texto));
            document.add(new Paragraph("Precio: $" + ticket.getPrecio(), texto));
            document.add(new Paragraph("Estado: " + ticket.getEstado(), texto));
            document.add(new Paragraph("Fecha: " + ticket.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), texto));

            // Cerrar el documento escribe el contenido final en el archivo
            document.close();
        }

    }

    /**
     * Lista los tickets de un evento.
     *
     * @param eventoId id del evento
     * @return lista de tickets (vacía si no hay)
     */
    public List<Ticket> listarPorEvento(String eventoId) {
        return ticketRepository.findByEventoId(eventoId);
    }

    /**
     * Lista los tickets de un usuario.
     *
     * @param usuarioId id del usuario
     * @return lista de tickets (vacía si no hay)
     */
    public List<Ticket> listarPorUsuario(String usuarioId) {
        return ticketRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Busca un ticket por su id.
     *
     * @param id id del ticket
     * @return el ticket encontrado
     * @throws TicketNotFoundException si no existe (se traduce en HTTP 404)
     */
    public Ticket obtenerPorId(Long id) {
        // findById devuelve un Optional; si viene vacío se lanza la excepción
        return ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    /**
     * Descarga desde S3 el PDF de un ticket.
     *
     * @param id id del ticket
     * @return el PDF como {@link Resource}, listo para enviarse en la respuesta HTTP
     * @throws TicketNotFoundException si el ticket no existe, nunca se subió a S3
     *                                 o el objeto ya no está en el bucket
     */
    public Resource obtenerPdf(Long id) {
        Ticket ticket = obtenerPorId(id);

        // El ticket existe pero su PDF nunca se subió a S3
        if (ticket.getRutaS3() == null) {
            throw new TicketNotFoundException("El ticket " + id + " no tiene un PDF en S3");
        }

        byte[] contenido = s3Service.descargarArchivo(ticket.getRutaS3());
        return new ByteArrayResource(contenido);
    }

    /**
     * Modifica el precio y/o el estado de un ticket, regenera su PDF en el EFS
     * y reemplaza el archivo en S3. Solo se actualizan los campos que vienen distintos de {@code null}.
     *
     * @param id      id del ticket
     * @param request nuevos valores
     * @return el ticket actualizado
     * @throws TicketNotFoundException si el ticket no existe o no tiene un PDF generado
     * @throws RuntimeException        si no se puede regenerar el PDF o subirlo a S3
     */
    public Ticket modificarTicket(Long id, TicketUpdateRequest request) {
        Ticket ticket = obtenerPorId(id);

        // Sin ruta registrada no hay PDF que regenerar (evita un NullPointerException en Paths.get)
        if (ticket.getRutaEfs() == null) {
            throw new TicketNotFoundException("El ticket " + id + " no tiene un PDF generado");
        }

        // Actualización parcial: solo se cambian los campos enviados
        if (request.getPrecio() != null) {
            ticket.setPrecio(request.getPrecio());
        }
        if (request.getEstado() != null) {
            ticket.setEstado(request.getEstado());
        }

        try {
            // Regenerar el PDF en la misma ruta para que refleje los cambios
            generarPdf(ticket, Paths.get(ticket.getRutaEfs()));
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("No se pudo actualizar el PDF del ticket", e);
        }

        // Se sube el PDF regenerado a S3 con la misma clave, reemplazando la versión anterior
        subirPdfAS3(ticket);

        return ticketRepository.save(ticket);
    }

    /**
     * Elimina un ticket: borra su PDF de S3 y del EFS, y luego el registro de la base de datos.
     *
     * @param id id del ticket
     * @throws TicketNotFoundException si el ticket no existe
     * @throws RuntimeException        si no se puede borrar el PDF en S3 o en el EFS
     */
    public void eliminarTicket(Long id) {
        Ticket ticket = obtenerPorId(id);

        // Borrar el objeto en S3
        if (ticket.getRutaS3() != null) {
            s3Service.eliminarArchivo(ticket.getRutaS3());
        }

        // Borrar el archivo en el EFS
        if (ticket.getRutaEfs() != null) {
            try {
                // deleteIfExists no falla si el archivo ya no estaba
                Files.deleteIfExists(Paths.get(ticket.getRutaEfs()));
            } catch (IOException e) {
                throw new RuntimeException("No se pudo eliminar el PDF del ticket", e);
            }
        }

        ticketRepository.delete(ticket);
    }

}
