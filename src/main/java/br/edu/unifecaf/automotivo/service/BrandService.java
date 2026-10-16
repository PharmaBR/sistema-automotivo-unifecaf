package br.edu.unifecaf.automotivo.service;

import br.edu.unifecaf.automotivo.domain.Brand;
import br.edu.unifecaf.automotivo.domain.BusinessRuleException;
import br.edu.unifecaf.automotivo.domain.ResourceNotFoundException;
import br.edu.unifecaf.automotivo.repository.BrandRepository;
import br.edu.unifecaf.automotivo.repository.VehicleModelRepository;
import br.edu.unifecaf.automotivo.web.dto.BrandRequest;
import br.edu.unifecaf.automotivo.web.dto.BrandResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Camada de serviço: orquestra o caso de uso (busca, valida unicidade, salva).
 * As regras do PRÓPRIO objeto ficam na entidade; aqui ficam as que dependem do banco.
 * @Transactional = tudo dentro do método é uma transação (commit/rollback automático).
 */
@Service
@Transactional
public class BrandService {

    private final BrandRepository brands;
    private final VehicleModelRepository models;

    // Injeção de dependência pelo construtor: o Spring entrega os repositórios prontos.
    public BrandService(BrandRepository brands, VehicleModelRepository models) {
        this.brands = brands;
        this.models = models;
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> findAll() {
        return brands.findAllByOrderByNameAsc().stream().map(BrandResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BrandResponse findById(Long id) {
        return BrandResponse.from(getOrThrow(id));
    }

    public BrandResponse create(BrandRequest request) {
        if (brands.existsByNameIgnoreCase(request.name().trim())) {
            throw new BusinessRuleException("Já existe uma marca chamada " + request.name().trim());
        }
        Brand brand = new Brand(request.name(), request.country());
        return BrandResponse.from(brands.save(brand));
    }

    public BrandResponse update(Long id, BrandRequest request) {
        Brand brand = getOrThrow(id);
        if (brands.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new BusinessRuleException("Já existe uma marca chamada " + request.name().trim());
        }
        brand.rename(request.name());
        brand.changeCountry(request.country());
        return BrandResponse.from(brand); // sem save(): a entidade gerenciada é salva no commit
    }

    public void delete(Long id) {
        Brand brand = getOrThrow(id);
        if (models.existsByBrandId(id)) {
            throw new BusinessRuleException("A marca possui modelos cadastrados e não pode ser removida");
        }
        brands.delete(brand);
    }

    Brand getOrThrow(Long id) {
        return brands.findById(id).orElseThrow(() -> new ResourceNotFoundException("Marca", id));
    }
}
