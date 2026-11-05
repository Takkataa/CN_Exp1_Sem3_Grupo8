package cl.duoc.tickets_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data 
public class TicketRequest {

    @NotBlank(message = "El eventoId es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "El eventoId solo puede tener letras, números, guion y guion bajo")
    private String eventoId;

    @NotBlank(message = "El usuarioId es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$",
             message = "El usuarioId solo puede tener letras, números, guion y guion bajo")
    private String usuarioId;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private Double precio;

}
