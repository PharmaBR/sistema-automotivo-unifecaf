package br.edu.unifecaf.automotivo.service;

import br.edu.unifecaf.automotivo.domain.ResourceNotFoundException;
import br.edu.unifecaf.automotivo.domain.Vehicle;
import br.edu.unifecaf.automotivo.domain.VehicleModel;
import br.edu.unifecaf.automotivo.domain.VehicleStatus;
import br.edu.unifecaf.automotivo.repository.VehicleFilter;
import br.edu.unifecaf.automotivo.repository.VehicleRepository;
import br.edu.unifecaf.automotivo.repository.VehicleSpecifications;
import br.edu.unifecaf.automotivo.web.dto.PageResponse;
import br.edu.unifecaf.automotivo.web.dto.VehicleCreateRequest;
import br.edu.unifecaf.automotivo.web.dto.VehicleResponse;
import br.edu.unifecaf.automotivo.web.dto.VehicleUpdateRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

/** Casos de uso do estoque de veículos (RF01, RF03, RF04, RF05). */
@Service
@Transactional
public class VehicleService {

    private final VehicleRepository vehicles;
    private final VehicleModelService modelService;

    public VehicleService(VehicleRepository vehicles, VehicleModelService modelService) {
        this.vehicles = vehicles;
        this.modelService = modelService;
    }

    /** RF03 — consulta com filtros combináveis + paginação/ordenação. */
    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> search(VehicleFilter filter, Pageable pageable) {
        filter.validate();
        var page = vehicles.findAll(VehicleSpecifications.matching(filter), pageable)
                .map(VehicleResponse::from);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public VehicleResponse findById(Long id) {
        return VehicleResponse.from(getOrThrow(id));
    }

    /** Painel do gerente: quantos veículos em cada status. */
    @Transactional(readOnly = true)
    public Map<VehicleStatus, Long> countByStatus() {
        Map<VehicleStatus, Long> summary = new EnumMap<>(VehicleStatus.class);
        for (VehicleStatus status : VehicleStatus.values()) {
            summary.put(status, vehicles.countByStatus(status));
        }
        return summary;
    }

    /** RF01 — cadastro. */
    public VehicleResponse create(VehicleCreateRequest request) {
        VehicleModel model = modelService.getOrThrow(request.modelId());
        Vehicle vehicle = new Vehicle(
                model,
                request.manufactureYear(),
                request.color(),
                request.price(),
                request.mileage());
        return VehicleResponse.from(vehicles.save(vehicle));
    }

    /** RF04 — atualização parcial. As regras ficam nos métodos da entidade. */
    public VehicleResponse update(Long id, VehicleUpdateRequest request) {
        Vehicle vehicle = getOrThrow(id);
        if (request.color() != null) {
            vehicle.changeColor(request.color());
        }
        if (request.price() != null) {
            vehicle.changePrice(request.price());
        }
        if (request.mileage() != null) {
            vehicle.updateMileage(request.mileage());
        }
        if (request.status() != null) { // por último: permite ajustar preço e marcar como vendido no mesmo PATCH
            vehicle.changeStatus(request.status());
        }
        // saveAndFlush força o UPDATE agora, para devolver o updatedAt já atualizado
        return VehicleResponse.from(vehicles.saveAndFlush(vehicle));
    }

    /** RF05 — remoção (somente vendidos ou descontinuados). */
    public void delete(Long id) {
        Vehicle vehicle = getOrThrow(id);
        vehicle.ensureCanBeRemoved();
        vehicles.delete(vehicle);
    }

    private Vehicle getOrThrow(Long id) {
        return vehicles.findById(id).orElseThrow(() -> new ResourceNotFoundException("Veículo", id));
    }
}
