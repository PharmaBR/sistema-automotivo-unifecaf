package br.edu.unifecaf.automotivo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Marca de veículo (Toyota, Fiat...). HERDA id e auditoria de BaseEntity.
 */
@Entity
@Table(name = "brands")
public class Brand extends BaseEntity {

    @Column(nullable = false, length = 80, unique = true)
    private String name;

    @Column(length = 60)
    private String country;

    /** Construtor vazio exigido pelo JPA; "protected" impede uso fora do framework. */
    protected Brand() {
    }

    public Brand(String name, String country) {
        rename(name);
        changeCountry(country);
    }

    /** ENCAPSULAMENTO: em vez de um setter cru, um método com regra. */
    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessRuleException("O nome da marca é obrigatório");
        }
        this.name = newName.trim();
    }

    public void changeCountry(String newCountry) {
        this.country = (newCountry == null || newCountry.isBlank()) ? null : newCountry.trim();
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }
}
