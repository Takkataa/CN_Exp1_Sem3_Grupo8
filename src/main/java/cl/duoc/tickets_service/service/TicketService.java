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

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

import javax.management.RuntimeErrorException;

@Service 
@RequiredArgsConstructor 
public class TicketService {

    private final TicketRepository ticketRepository;

    @Value("${efs.path}")
    private String efsPath;

    public Ticket generarTicket(TicketRequest request) {
        //se crea para tener su id
        Ticket ticket = new Ticket();
        ticket.setEventoId(request.getEventoId());
        ticket.setUsuarioId(request.getUsuarioId());
        ticket.setPrecio(request.getPrecio());
        ticket = ticketRepository.save(ticket);

        try {
            //Se nombra el archivo y la carpeta
            String nombreArchivo = String.format("ticket-%06d.pdf", ticket.getId());
            Path carpeta = Paths.get(efsPath, request.getEventoId(), request.getUsuarioId());

            //Crea la carpeta si no existe
            Files.createDirectories(carpeta);

            //Se genera el pdf
            Path rutaFinal = carpeta.resolve(nombreArchivo);
            generarPdf(ticket, rutaFinal);

            //Se actualiza el ticket en la base de datos con las rutas
            ticket.setNombreArchivo(nombreArchivo);
            ticket.setRutaEfs(rutaFinal.toString());
            
            return ticketRepository.save(ticket);
            
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("No se pudo generar el PDF del ticket", e);
        }
    }

    private void generarPdf(Ticket ticket, Path rutaArchivo) throws IOException, DocumentException{

        Document document = new Document();
        try (FileOutputStream fos = new FileOutputStream(rutaArchivo.toFile())){

            PdfWriter.getInstance(document, fos);
            document.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD,20);
            Font texto = FontFactory.getFont(FontFactory.HELVETICA, 12);

            Paragraph encabezado = new Paragraph("TICKET DEL VENTO", titulo);
            encabezado.setAlignment(Element.ALIGN_CENTER);
            encabezado.setSpacingAfter(20);
            document.add(encabezado);

            document.add(new Paragraph("Ticket N°: " + ticket.getId(), texto));
            document.add(new Paragraph("Evento: " + ticket.getEventoId(), texto));
            document.add(new Paragraph("Usuario: " + ticket.getUsuarioId(), texto));
            document.add(new Paragraph("Precio: $" + ticket.getPrecio(), texto));
            document.add(new Paragraph("Fecha: " + ticket.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:MM")), texto));

            document.close();
        }

    }
}
