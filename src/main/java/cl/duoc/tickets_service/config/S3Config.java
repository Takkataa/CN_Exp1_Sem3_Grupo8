package cl.duoc.tickets_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Configuración del cliente de Amazon S3.
 *
 * <p>Las credenciales no se escriben en el código: el SDK las busca automáticamente
 * con su "cadena de credenciales por defecto", en este orden (entre otros):
 * <ol>
 *   <li>Variables de entorno {@code AWS_ACCESS_KEY_ID}, {@code AWS_SECRET_ACCESS_KEY}
 *       y {@code AWS_SESSION_TOKEN} (útil en local con el laboratorio de AWS Academy).</li>
 *   <li>Archivo {@code ~/.aws/credentials}.</li>
 *   <li>Rol IAM asociado a la instancia EC2 (por ejemplo, LabRole), ideal en producción.</li>
 * </ol>
 */
@Configuration // Clase que define beans para el contexto de Spring
public class S3Config {

    /** Región de AWS donde está el bucket. Se lee de {@code aws.region}. */
    @Value("${aws.region}")
    private String region;

    /**
     * Crea el cliente de S3 como bean, para inyectarlo donde se necesite
     * (Spring crea una sola instancia y la reutiliza).
     *
     * @return cliente de S3 configurado con la región indicada
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }
}
