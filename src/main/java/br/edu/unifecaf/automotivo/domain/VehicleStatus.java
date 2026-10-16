package br.edu.unifecaf.automotivo.domain;

/**
 * Status de disponibilidade do veículo.
 * Em Java, um enum é uma classe de verdade: pode ter atributos e métodos.
 * Aqui ele concentra as regras de transição de status (quem pode virar o quê).
 */
public enum VehicleStatus {

    AVAILABLE("Disponível"),
    RESERVED("Reservado"),
    SOLD("Vendido"),
    DISCONTINUED("Descontinuado");

    private final String label;

    VehicleStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Regras de transição:
     * - Disponível e Reservado podem ir para qualquer status;
     * - Descontinuado só pode voltar a Disponível (reativação);
     * - Vendido é final: um carro vendido não volta ao estoque.
     */
    public boolean canTransitionTo(VehicleStatus target) {
        if (this == target) {
            return true;
        }
        return switch (this) {
            case AVAILABLE, RESERVED -> true;
            case DISCONTINUED -> target == AVAILABLE;
            case SOLD -> false;
        };
    }

    /** Requisito funcional 5: só remove do estoque quando vendido ou descontinuado. */
    public boolean allowsRemoval() {
        return this == SOLD || this == DISCONTINUED;
    }

    /** Um veículo vendido não pode mais ter preço/quilometragem/cor editados. */
    public boolean allowsEditing() {
        return this != SOLD;
    }
}
