package br.edu.unifecaf.automotivo.web.dto;

import br.edu.unifecaf.automotivo.domain.Vehicle;
import br.edu.unifecaf.automotivo.domain.VehicleCategory;
import br.edu.unifecaf.automotivo.domain.VehicleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VehicleResponse(
        Long id,
        Long brandId,
        String brand,
        Long modelId,
        String model,
        VehicleCategory category,
        Integer manufactureYear,
        String color,
        BigDecimal price,
        Integer mileage,
        VehicleStatus status,
        String statusLabel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static VehicleResponse from(Vehicle vehicle) {
        var model = vehicle.getModel();
        var brand = model.getBrand();
        return new VehicleResponse(
                vehicle.getId(),
                brand.getId(),
                brand.getName(),
                model.getId(),
                model.getName(),
                model.getCategory(),
                vehicle.getManufactureYear(),
                vehicle.getColor(),
                vehicle.getPrice(),
                vehicle.getMileage(),
                vehicle.getStatus(),
                vehicle.getStatus().getLabel(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt());
    }
}
