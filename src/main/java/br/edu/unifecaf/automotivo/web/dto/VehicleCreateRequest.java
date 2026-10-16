package br.edu.unifecaf.automotivo.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Cadastro de veículo (RF01). A marca vem do modelo escolhido. */
public record VehicleCreateRequest(
        @NotNull(message = "é obrigatório") Long modelId,
        @NotNull(message = "é obrigatório") @Min(1900) @Max(2100) Integer manufactureYear,
        @NotBlank(message = "é obrigatório") @Size(max = 40) String color,
        @NotNull(message = "é obrigatório") @DecimalMin(value = "0.01", message = "deve ser maior que zero")
        @Digits(integer = 10, fraction = 2) BigDecimal price,
        @NotNull(message = "é obrigatório") @PositiveOrZero Integer mileage
) {
}
