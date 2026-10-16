package br.edu.unifecaf.automotivo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação (equivale ao manage.py runserver / uvicorn main:app).
 * A anotação @SpringBootApplication liga a autoconfiguração e a varredura de componentes
 * (@Service, @RestController, @Repository...) a partir deste pacote.
 */
@SpringBootApplication
public class AutomotivoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutomotivoApplication.class, args);
    }
}
