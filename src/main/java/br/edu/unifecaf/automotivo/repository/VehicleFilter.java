package br.edu.unifecaf.automotivo.repository;

import br.edu.unifecaf.automotivo.domain.VehicleCategory;
import br.edu.unifecaf.automotivo.domain.VehicleStatus;

import java.math.BigDecimal;

/**
 * Critérios de busca de veículos. Todos opcionais: campo nulo = sem filtro.
 * Um "record" é uma classe imutável compacta (parecido com @dataclass(frozen=True)).
 * O Spring preenche este objeto direto da query string: ?brandId=1&minPrice=50000...
 */
public record VehicleFilter(
        Long brandId,
        Long modelId,
        String brand,
        String model,
        VehicleCategory category,
        VehicleStatus status,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer minYear,
        Integer maxYear,
        String color
) {

    public static VehicleFilter empty() {
        return new VehicleFilter(null, null, null, null, null, null, null, null, null, null, null);
    }

    /** Valida combinações incoerentes (ex.: preço mínimo maior que o máximo). */
    public void validate() {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice não pode ser maior que maxPrice");
        }
        if (minYear != null && maxYear != null && minYear > maxYear) {
            throw new IllegalArgumentException("minYear não pode ser maior que maxYear");
        }
    }
}
