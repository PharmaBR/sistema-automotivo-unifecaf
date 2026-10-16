package br.edu.unifecaf.automotivo.service;

import br.edu.unifecaf.automotivo.domain.Brand;
import br.edu.unifecaf.automotivo.domain.BusinessRuleException;
import br.edu.unifecaf.automotivo.domain.ResourceNotFoundException;
import br.edu.unifecaf.automotivo.domain.VehicleModel;
import br.edu.unifecaf.automotivo.repository.VehicleModelRepository;
import br.edu.unifecaf.automotivo.repository.VehicleRepository;
import br.edu.unifecaf.automotivo.web.dto.VehicleModelRequest;
import br.edu.unifecaf.automotivo.web.dto.VehicleModelResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class VehicleModelService {

    private final VehicleModelRepository models;
    private final VehicleRepository vehicles;
    private final BrandService brandService;

    public VehicleModelService(VehicleModelRepository models, VehicleRepository vehicles, BrandService brandService) {
        this.models = models;
        this.vehicles = vehicles;
        this.brandService = brandService;
    }

    @Transactional(readOnly = true)
    public List<VehicleModelResponse> findAll(Long brandId) {
        List<VehicleModel> result = (brandId == null)
                ? models.findAllByOrderByNameAsc()
                : models.findByBrandIdOrderByNameAsc(brandId);
        return result.stream().map(VehicleModelResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public VehicleModelResponse findById(Long id) {
        return VehicleModelResponse.from(getOrThrow(id));
    }

    public VehicleModelResponse create(VehicleModelRequest request) {
        Brand brand = brandService.getOrThrow(request.brandId());
        if (models.existsByBrandIdAndNameIgnoreCase(brand.getId(), request.name().trim())) {
            throw new BusinessRuleException("O modelo " + request.name().trim() + " já existe para a marca " + brand.getName());
        }
        VehicleModel model = new VehicleModel(request.name(), request.category(), brand);
        return VehicleModelResponse.from(models.save(model));
    }

    public VehicleModelResponse update(Long id, VehicleModelRequest request) {
        VehicleModel model = getOrThrow(id);
        Brand brand = brandService.getOrThrow(request.brandId());
        if (models.existsByBrandIdAndNameIgnoreCaseAndIdNot(brand.getId(), request.name().trim(), id)) {
            throw new BusinessRuleException("O modelo " + request.name().trim() + " já existe para a marca " + brand.getName());
        }
        model.rename(request.name());
        model.changeCategory(request.category());
        model.changeBrand(brand);
        return VehicleModelResponse.from(model);
    }

    public void delete(Long id) {
        VehicleModel model = getOrThrow(id);
        if (vehicles.existsByModelId(id)) {
            throw new BusinessRuleException("O modelo possui veículos no estoque e não pode ser removido");
        }
        models.delete(model);
    }

    VehicleModel getOrThrow(Long id) {
        return models.findById(id).orElseThrow(() -> new ResourceNotFoundException("Modelo", id));
    }
}
