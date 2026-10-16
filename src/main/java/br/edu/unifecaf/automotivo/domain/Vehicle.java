package br.edu.unifecaf.automotivo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.Objects;

/**
 * Veículo em estoque — a entidade central do sistema.
 *
 * ENCAPSULAMENTO na prática: todos os atributos são private e NÃO existem setters.
 * Toda mudança passa por um método com nome de negócio (changePrice, updateMileage,
 * changeStatus...) que valida a regra antes de alterar o estado. Assim é impossível
 * um veículo ficar com preço negativo ou quilometragem "voltando", venha a chamada
 * da API, do front-end ou de um teste.
 *
 * A marca não é guardada aqui: o veículo aponta para o modelo, e o modelo para a marca
 * (Vehicle -> VehicleModel -> Brand), evitando dados duplicados/inconsistentes.
 */
@Entity
@Table(name = "vehicles")
public class Vehicle extends BaseEntity {

    private static final int OLDEST_ACCEPTED_YEAR = 1900;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private VehicleModel model;

    @Column(name = "manufacture_year", nullable = false)
    private Integer manufactureYear;

    @Column(nullable = false, length = 40)
    private String color;

    @Column(nullable = false, precision = 12, scale = 2) // BigDecimal: nunca use double para dinheiro
    private BigDecimal price;

    @Column(nullable = false)
    private Integer mileage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status;

    protected Vehicle() {
    }

    /** Todo veículo novo entra no estoque como DISPONÍVEL. */
    public Vehicle(VehicleModel model, int manufactureYear, String color, BigDecimal price, int mileage) {
        this.status = VehicleStatus.AVAILABLE;
        this.model = Objects.requireNonNull(model, "Modelo é obrigatório");
        this.manufactureYear = validateYear(manufactureYear);
        changeColor(color);
        changePrice(price);
        updateMileage(mileage);
    }

    // ------------------------------------------------------------------
    // Comportamentos (métodos de negócio)
    // ------------------------------------------------------------------

    public void changePrice(BigDecimal newPrice) {
        ensureEditable();
        if (newPrice == null || newPrice.signum() <= 0) {
            throw new BusinessRuleException("O preço deve ser maior que zero");
        }
        this.price = newPrice.setScale(2, RoundingMode.HALF_UP);
    }

    public void updateMileage(int newMileage) {
        ensureEditable();
        if (newMileage < 0) {
            throw new BusinessRuleException("A quilometragem não pode ser negativa");
        }
        if (this.mileage != null && newMileage < this.mileage) {
            throw new BusinessRuleException(
                    "A quilometragem não pode diminuir (atual: " + this.mileage + " km)");
        }
        this.mileage = newMileage;
    }

    public void changeColor(String newColor) {
        ensureEditable();
        if (newColor == null || newColor.isBlank()) {
            throw new BusinessRuleException("A cor é obrigatória");
        }
        this.color = newColor.trim();
    }

    public void changeStatus(VehicleStatus newStatus) {
        Objects.requireNonNull(newStatus, "Status é obrigatório");
        if (!status.canTransitionTo(newStatus)) {
            throw new BusinessRuleException(
                    "Não é permitido mudar o status de " + status.getLabel() + " para " + newStatus.getLabel());
        }
        this.status = newStatus;
    }

    public void markAsSold() {
        changeStatus(VehicleStatus.SOLD);
    }

    public boolean canBeRemoved() {
        return status.allowsRemoval();
    }

    /** Lança exceção se o veículo ainda está em estoque ativo (regra do requisito 5). */
    public void ensureCanBeRemoved() {
        if (!canBeRemoved()) {
            throw new BusinessRuleException(
                    "Só é possível remover veículos vendidos ou descontinuados (status atual: "
                            + status.getLabel() + ")");
        }
    }

    // ------------------------------------------------------------------
    // Métodos auxiliares privados (detalhes internos escondidos)
    // ------------------------------------------------------------------

    private void ensureEditable() {
        if (status != null && !status.allowsEditing()) {
            throw new BusinessRuleException("Veículo vendido não pode ser editado");
        }
    }

    private static int validateYear(int year) {
        int maxYear = Year.now().getValue() + 1; // ano-modelo pode ser o próximo
        if (year < OLDEST_ACCEPTED_YEAR || year > maxYear) {
            throw new BusinessRuleException(
                    "Ano de fabricação deve estar entre " + OLDEST_ACCEPTED_YEAR + " e " + maxYear);
        }
        return year;
    }

    // ------------------------------------------------------------------
    // Getters (somente leitura)
    // ------------------------------------------------------------------

    public VehicleModel getModel() {
        return model;
    }

    public Brand getBrand() {
        return model.getBrand();
    }

    public Integer getManufactureYear() {
        return manufactureYear;
    }

    public String getColor() {
        return color;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getMileage() {
        return mileage;
    }

    public VehicleStatus getStatus() {
        return status;
    }
}
