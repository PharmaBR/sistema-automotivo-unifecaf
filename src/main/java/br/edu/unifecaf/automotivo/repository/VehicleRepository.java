package br.edu.unifecaf.automotivo.repository;

import br.edu.unifecaf.automotivo.domain.Vehicle;
import br.edu.unifecaf.automotivo.domain.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * JpaSpecificationExecutor permite montar consultas dinâmicas (filtros opcionais)
 * com a Criteria API — ver VehicleSpecifications.
 */
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {

    @Override
    @EntityGraph(attributePaths = {"model", "model.brand"})
    Page<Vehicle> findAll(Specification<Vehicle> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"model", "model.brand"})
    Optional<Vehicle> findById(Long id);

    boolean existsByModelId(Long modelId);

    long countByStatus(VehicleStatus status);
}
