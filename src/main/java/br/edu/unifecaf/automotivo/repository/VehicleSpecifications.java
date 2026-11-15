package br.edu.unifecaf.automotivo.repository;

import br.edu.unifecaf.automotivo.domain.Brand;
import br.edu.unifecaf.automotivo.domain.Vehicle;
import br.edu.unifecaf.automotivo.domain.VehicleModel;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Monta o WHERE dinamicamente: só entra na consulta o filtro que foi informado.
 * Equivale a ir encadeando .filter() num QuerySet do Django.
 */
public final class VehicleSpecifications {

    /** Caractere de escape do LIKE: faz % e _ digitados pelo usuário valerem como texto comum. */
    private static final char ESCAPE = '!';

    private VehicleSpecifications() {
        // classe utilitária: não deve ser instanciada
    }

    public static Specification<Vehicle> matching(VehicleFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Vehicle, VehicleModel> model = root.join("model");
            Join<VehicleModel, Brand> brand = model.join("brand");

            if (filter.brandId() != null) {
                predicates.add(cb.equal(brand.get("id"), filter.brandId()));
            }
            if (filter.modelId() != null) {
                predicates.add(cb.equal(model.get("id"), filter.modelId()));
            }
            if (hasText(filter.brand())) {
                predicates.add(cb.like(cb.lower(brand.get("name")), contains(filter.brand()), ESCAPE));
            }
            if (hasText(filter.model())) {
                predicates.add(cb.like(cb.lower(model.get("name")), contains(filter.model()), ESCAPE));
            }
            if (filter.category() != null) {
                predicates.add(cb.equal(model.get("category"), filter.category()));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
            }
            if (filter.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
            }
            if (filter.minYear() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("manufactureYear"), filter.minYear()));
            }
            if (filter.maxYear() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("manufactureYear"), filter.maxYear()));
            }
            if (hasText(filter.color())) {
                predicates.add(cb.like(cb.lower(root.get("color")), contains(filter.color()), ESCAPE));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String contains(String value) {
        String escaped = value.trim().toLowerCase(Locale.ROOT)
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
        return "%" + escaped + "%";
    }
}
