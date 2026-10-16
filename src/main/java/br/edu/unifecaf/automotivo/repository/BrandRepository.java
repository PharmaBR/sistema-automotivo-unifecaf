package br.edu.unifecaf.automotivo.repository;

import br.edu.unifecaf.automotivo.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data gera a implementação sozinho a partir do NOME do método
 * (como Brand.objects.filter(...) no Django, mas declarado numa interface).
 */
public interface BrandRepository extends JpaRepository<Brand, Long> {

    List<Brand> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
