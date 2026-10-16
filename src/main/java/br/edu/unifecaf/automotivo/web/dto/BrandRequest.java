package br.edu.unifecaf.automotivo.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corpo do POST/PUT de marca. As anotações validam a entrada (como um Serializer do DRF). */
public record BrandRequest(
        @NotBlank(message = "é obrigatório") @Size(max = 80) String name,
        @Size(max = 60) String country
) {
}
