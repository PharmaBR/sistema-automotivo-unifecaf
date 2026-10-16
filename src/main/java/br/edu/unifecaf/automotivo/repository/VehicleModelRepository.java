package br.edu.unifecaf.automotivo.repository;

import br.edu.unifecaf.automotivo.domain.VehicleModel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {

    // @EntityGraph carrega a marca na mesma consulta (JOIN), evitando o problema N+1
    // — o equivalente ao select_related("brand") do Django.
    @EntityGraph(attributePaths = "brand")
    List<VehicleModel> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = "brand")
    List<VehicleModel> findByBrandIdOrderByNameAsc(Long brandId);

    boolean existsByBrandId(Long brandId);

    boolean existsByBrandIdAndNameIgnoreCase(Long brandId, String name);

    boolean existsByBrandIdAndNameIgnoreCaseAndIdNot(Long brandId, String name, Long id);
}
