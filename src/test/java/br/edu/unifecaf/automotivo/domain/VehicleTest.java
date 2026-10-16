package br.edu.unifecaf.automotivo.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de UNIDADE do domínio: Java puro, sem Spring e sem banco — rodam em milissegundos.
 * Cada teste documenta uma regra de negócio da entidade Vehicle.
 */
@DisplayName("Vehicle (regras de negócio)")
class VehicleTest {

    private VehicleModel corolla;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        corolla = new VehicleModel("Corolla", VehicleCategory.SEDAN, new Brand("Toyota", "Japão"));
        vehicle = new Vehicle(corolla, 2022, "Prata", new BigDecimal("120000"), 30000);
    }

    @Nested
    @DisplayName("Cadastro")
    class Creation {

        @Test
        @DisplayName("todo veículo novo entra como DISPONÍVEL")
        void newVehicleStartsAvailable() {
            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
            assertThat(vehicle.getBrand().getName()).isEqualTo("Toyota");
            assertThat(vehicle.getPrice()).isEqualByComparingTo("120000.00");
        }

        @Test
        @DisplayName("rejeita ano de fabricação fora do intervalo aceito")
        void rejectsInvalidYear() {
            int tooFarAhead = Year.now().getValue() + 2;
            assertThatThrownBy(() -> new Vehicle(corolla, tooFarAhead, "Prata", BigDecimal.TEN, 0))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Ano de fabricação");
            assertThatThrownBy(() -> new Vehicle(corolla, 1899, "Prata", BigDecimal.TEN, 0))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("rejeita cor em branco")
        void rejectsBlankColor() {
            assertThatThrownBy(() -> new Vehicle(corolla, 2022, "  ", BigDecimal.TEN, 0))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    @Nested
    @DisplayName("Preço")
    class Price {

        @Test
        @DisplayName("aceita novo preço positivo e arredonda para 2 casas")
        void changesPrice() {
            vehicle.changePrice(new BigDecimal("115000.499"));
            assertThat(vehicle.getPrice()).isEqualByComparingTo("115000.50");
        }

        @Test
        @DisplayName("rejeita preço zero ou negativo")
        void rejectsNonPositivePrice() {
            assertThatThrownBy(() -> vehicle.changePrice(BigDecimal.ZERO))
                    .isInstanceOf(BusinessRuleException.class);
            assertThatThrownBy(() -> vehicle.changePrice(new BigDecimal("-1")))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    @Nested
    @DisplayName("Quilometragem")
    class Mileage {

        @Test
        @DisplayName("pode aumentar")
        void canIncrease() {
            vehicle.updateMileage(31000);
            assertThat(vehicle.getMileage()).isEqualTo(31000);
        }

        @Test
        @DisplayName("não pode diminuir (hodômetro não volta)")
        void cannotDecrease() {
            assertThatThrownBy(() -> vehicle.updateMileage(29000))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("não pode diminuir");
        }
    }

    @Nested
    @DisplayName("Status e remoção")
    class StatusAndRemoval {

        @Test
        @DisplayName("veículo vendido não volta ao estoque nem pode ser editado")
        void soldIsFinal() {
            vehicle.markAsSold();

            assertThatThrownBy(() -> vehicle.changeStatus(VehicleStatus.AVAILABLE))
                    .isInstanceOf(BusinessRuleException.class);
            assertThatThrownBy(() -> vehicle.changePrice(new BigDecimal("1000")))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("vendido");
        }

        @Test
        @DisplayName("descontinuado pode ser reativado")
        void discontinuedCanBeReactivated() {
            vehicle.changeStatus(VehicleStatus.DISCONTINUED);
            vehicle.changeStatus(VehicleStatus.AVAILABLE);
            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        }

        @Test
        @DisplayName("só pode ser removido se vendido ou descontinuado")
        void removalRule() {
            assertThat(vehicle.canBeRemoved()).isFalse();
            assertThatThrownBy(vehicle::ensureCanBeRemoved).isInstanceOf(BusinessRuleException.class);

            vehicle.changeStatus(VehicleStatus.RESERVED);
            assertThat(vehicle.canBeRemoved()).isFalse();

            vehicle.markAsSold();
            assertThat(vehicle.canBeRemoved()).isTrue();
        }
    }
}
