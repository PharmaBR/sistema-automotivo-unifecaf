package br.edu.unifecaf.automotivo.web.dto;

import br.edu.unifecaf.automotivo.domain.VehicleCategory;
import br.edu.unifecaf.automotivo.domain.VehicleModel;

public record VehicleModelResponse(
        Long id,
        String name,
        VehicleCategory category,
        String categoryLabel,
        Long brandId,
        String brandName
) {

    public static VehicleModelResponse from(VehicleModel model) {
        return new VehicleModelResponse(
                model.getId(),
                model.getName(),
                model.getCategory(),
                model.getCategory().getLabel(),
                model.getBrand().getId(),
                model.getBrand().getName());
    }
}
