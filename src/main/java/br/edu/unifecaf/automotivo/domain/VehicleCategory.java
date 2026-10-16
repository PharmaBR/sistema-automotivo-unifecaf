package br.edu.unifecaf.automotivo.domain;

/** Tipo de veículo (usado no filtro "tipo de veículo" do desafio). Pertence ao modelo. */
public enum VehicleCategory {

    HATCH("Hatch"),
    SEDAN("Sedã"),
    SUV("SUV"),
    PICKUP("Picape"),
    MINIVAN("Minivan"),
    COUPE("Cupê");

    private final String label;

    VehicleCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
