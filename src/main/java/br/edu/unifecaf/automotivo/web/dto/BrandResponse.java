package br.edu.unifecaf.automotivo.web.dto;

import br.edu.unifecaf.automotivo.domain.Brand;

/** O que a API devolve: nunca expomos a entidade JPA diretamente. */
public record BrandResponse(Long id, String name, String country) {

    public static BrandResponse from(Brand brand) {
        return new BrandResponse(brand.getId(), brand.getName(), brand.getCountry());
    }
}
