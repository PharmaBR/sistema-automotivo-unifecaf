package br.edu.unifecaf.automotivo.web.dto;

import br.edu.unifecaf.automotivo.domain.VehicleStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Atualização parcial (PATCH) — RF04: preço, quilometragem, status (e cor).
 * Campo ausente/nulo = não altera.
 */
public record VehicleUpdateRequest(
        @DecimalMin(value = "0.01", message = "deve ser maior que zero") @Digits(integer = 10, fraction = 2)
        BigDecimal price,
        @PositiveOrZero Integer mileage,
        VehicleStatus status,
        @Size(max = 40) String color
) {
}
