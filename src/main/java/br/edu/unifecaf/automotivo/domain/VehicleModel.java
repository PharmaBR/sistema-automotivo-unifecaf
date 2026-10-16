package br.edu.unifecaf.automotivo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Modelo de veículo (Corolla, Onix...). Cada modelo pertence a UMA marca (N:1).
 * Nome "VehicleModel" evita confusão com o "Model" do padrão MVC.
 */
@Entity
@Table(name = "vehicle_models")
public class VehicleModel extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING) // grava "SUV", "SEDAN"... em vez de 0, 1, 2
    @Column(nullable = false, length = 20)
    private VehicleCategory category;

    // Relacionamento N:1 -> coluna brand_id (equivale a ForeignKey no Django)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    protected VehicleModel() {
    }

    public VehicleModel(String name, VehicleCategory category, Brand brand) {
        rename(name);
        changeCategory(category);
        changeBrand(brand);
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessRuleException("O nome do modelo é obrigatório");
        }
        this.name = newName.trim();
    }

    public void changeCategory(VehicleCategory newCategory) {
        this.category = Objects.requireNonNull(newCategory, "Categoria é obrigatória");
    }

    public void changeBrand(Brand newBrand) {
        this.brand = Objects.requireNonNull(newBrand, "Marca é obrigatória");
    }

    /** Ex.: "Toyota Corolla". */
    public String fullName() {
        return brand.getName() + " " + name;
    }

    public String getName() {
        return name;
    }

    public VehicleCategory getCategory() {
        return category;
    }

    public Brand getBrand() {
        return brand;
    }
}
