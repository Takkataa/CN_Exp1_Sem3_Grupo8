package cl.duoc.tickets_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del microservicio de tickets.
 *
 * <p>{@code @SpringBootApplication} combina tres anotaciones:
 * <ul>
 *   <li>{@code @Configuration}: permite definir beans en esta clase.</li>
 *   <li>{@code @EnableAutoConfiguration}: Spring Boot configura automáticamente
 *       los componentes según las dependencias del pom.xml (web, JPA, etc.).</li>
 *   <li>{@code @ComponentScan}: busca controladores, servicios y repositorios
 *       dentro del paquete {@code cl.duoc.tickets_service} y sus subpaquetes.</li>
 * </ul>
 */
@SpringBootApplication
public class TicketsServiceApplication {

	/**
	 * Punto de entrada de la aplicación. Levanta el contexto de Spring
	 * y el servidor web embebido (Tomcat).
	 *
	 * @param args argumentos de línea de comandos
	 */
	public static void main(String[] args) {
		SpringApplication.run(TicketsServiceApplication.class, args);
	}

}
