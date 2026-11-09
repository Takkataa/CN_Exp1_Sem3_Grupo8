package cl.duoc.tickets_service.service;

import cl.duoc.tickets_service.exception.TicketNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.file.Path;

/**
 * Servicio que encapsula las operaciones con Amazon S3:
 * subir, descargar y eliminar los PDF de los tickets.
 *
 * <p>Los archivos se organizan en el bucket por evento y usuario, por ejemplo:
 * {@code evento123/usuarioX/ticket-000001.pdf}.
 */
@Service
@RequiredArgsConstructor
public class S3Service {

    /** Cliente de S3 creado en {@link cl.duoc.tickets_service.config.S3Config}. */
    private final S3Client s3Client;

    /** Nombre del bucket donde se guardan los tickets. Se lee de {@code aws.s3.bucket}. */
    @Value("${aws.s3.bucket}")
    private String bucket;

    /**
     * Construye la clave (ruta dentro del bucket) de un ticket.
     * No empieza con "/" porque en S3 eso crearía una carpeta sin nombre.
     *
     * @param eventoId      id del evento (primera carpeta)
     * @param usuarioId     id del usuario (segunda carpeta)
     * @param nombreArchivo nombre del PDF
     * @return clave del objeto, por ejemplo {@code evento123/usuarioX/ticket-000001.pdf}
     */
    public String construirKey(String eventoId, String usuarioId, String nombreArchivo) {
        return eventoId + "/" + usuarioId + "/" + nombreArchivo;
    }

    /**
     * Sube (o reemplaza, si ya existe) un archivo PDF en S3.
     *
     * @param key     clave del objeto dentro del bucket
     * @param archivo ruta local del archivo a subir (en el EFS)
     * @throws RuntimeException si S3 rechaza la operación
     */
    public void subirArchivo(String key, Path archivo) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("application/pdf")
                    .build();

            // Si la clave ya existe, S3 sobrescribe el objeto (así se "actualiza")
            s3Client.putObject(request, RequestBody.fromFile(archivo));
        } catch (S3Exception e) {
            throw new RuntimeException("No se pudo subir el archivo a S3: " + key, e);
        }
    }

    /**
     * Descarga un archivo desde S3 y lo devuelve como arreglo de bytes.
     *
     * @param key clave del objeto dentro del bucket
     * @return contenido del archivo
     * @throws TicketNotFoundException si el objeto no existe en el bucket (HTTP 404)
     * @throws RuntimeException        si S3 rechaza la operación por otro motivo
     */
    public byte[] descargarArchivo(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            return s3Client.getObjectAsBytes(request).asByteArray();
        } catch (NoSuchKeyException e) {
            throw new TicketNotFoundException("El archivo no existe en S3: " + key);
        } catch (S3Exception e) {
            throw new RuntimeException("No se pudo descargar el archivo desde S3: " + key, e);
        }
    }

    /**
     * Elimina un archivo de S3. Si el objeto no existe, S3 no arroja error.
     *
     * @param key clave del objeto dentro del bucket
     * @throws RuntimeException si S3 rechaza la operación
     */
    public void eliminarArchivo(String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
        } catch (S3Exception e) {
            throw new RuntimeException("No se pudo eliminar el archivo de S3: " + key, e);
        }
    }
}
