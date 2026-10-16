package br.edu.unifecaf.automotivo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadados exibidos no Swagger UI (http://localhost:8080/swagger-ui.html). */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI automotivoOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Sistema Automotivo — Gestão de Estoque de Veículos")
                .version("1.0.0")
                .description("API REST para cadastro, consulta, atualização e remoção de veículos, "
                        + "marcas e modelos. Projeto acadêmico — UniFECAF / ADS."));
    }
}
