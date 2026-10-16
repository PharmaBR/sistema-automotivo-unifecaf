package br.edu.unifecaf.automotivo.web.dto;

import br.edu.unifecaf.automotivo.domain.VehicleCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehicleModelRequest(
        @NotBlank(message = "é obrigatório") @Size(max = 80) String name,
        @NotNull(message = "é obrigatório") VehicleCategory category,
        @NotNull(message = "é obrigatório") Long brandId
) {
}
