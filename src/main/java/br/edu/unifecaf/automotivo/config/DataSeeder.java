package br.edu.unifecaf.automotivo.config;

import br.edu.unifecaf.automotivo.domain.Brand;
import br.edu.unifecaf.automotivo.domain.Vehicle;
import br.edu.unifecaf.automotivo.domain.VehicleCategory;
import br.edu.unifecaf.automotivo.domain.VehicleModel;
import br.edu.unifecaf.automotivo.domain.VehicleStatus;
import br.edu.unifecaf.automotivo.repository.BrandRepository;
import br.edu.unifecaf.automotivo.repository.VehicleModelRepository;
import br.edu.unifecaf.automotivo.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Popula o banco com dados de exemplo na PRIMEIRA execução (banco vazio),
 * para a demonstração já começar com estoque. Não roda nos testes (perfil "test").
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final BrandRepository brands;
    private final VehicleModelRepository models;
    private final VehicleRepository vehicles;

    public DataSeeder(BrandRepository brands, VehicleModelRepository models, VehicleRepository vehicles) {
        this.brands = brands;
        this.models = models;
        this.vehicles = vehicles;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (brands.count() > 0) {
            return;
        }
        Map<String, Brand> b = new HashMap<>();
        for (String[] row : new String[][]{
                {"Toyota", "Japão"}, {"Volkswagen", "Alemanha"}, {"Fiat", "Itália"},
                {"Chevrolet", "Estados Unidos"}, {"Hyundai", "Coreia do Sul"}, {"Honda", "Japão"}}) {
            b.put(row[0], brands.save(new Brand(row[0], row[1])));
        }

        Map<String, VehicleModel> m = new HashMap<>();
        m.put("Corolla", model("Corolla", VehicleCategory.SEDAN, b.get("Toyota")));
        m.put("Hilux", model("Hilux", VehicleCategory.PICKUP, b.get("Toyota")));
        m.put("Polo", model("Polo", VehicleCategory.HATCH, b.get("Volkswagen")));
        m.put("T-Cross", model("T-Cross", VehicleCategory.SUV, b.get("Volkswagen")));
        m.put("Strada", model("Strada", VehicleCategory.PICKUP, b.get("Fiat")));
        m.put("Pulse", model("Pulse", VehicleCategory.SUV, b.get("Fiat")));
        m.put("Onix", model("Onix", VehicleCategory.HATCH, b.get("Chevrolet")));
        m.put("Tracker", model("Tracker", VehicleCategory.SUV, b.get("Chevrolet")));
        m.put("HB20", model("HB20", VehicleCategory.HATCH, b.get("Hyundai")));
        m.put("Creta", model("Creta", VehicleCategory.SUV, b.get("Hyundai")));
        m.put("Civic", model("Civic", VehicleCategory.SEDAN, b.get("Honda")));
        m.put("HR-V", model("HR-V", VehicleCategory.SUV, b.get("Honda")));

        vehicle(m.get("Corolla"), 2023, "Prata", "149900.00", 18500, VehicleStatus.AVAILABLE);
        vehicle(m.get("Corolla"), 2021, "Branco", "118500.00", 52300, VehicleStatus.AVAILABLE);
        vehicle(m.get("Hilux"), 2022, "Preto", "259900.00", 41000, VehicleStatus.RESERVED);
        vehicle(m.get("Polo"), 2024, "Vermelho", "92900.00", 6200, VehicleStatus.AVAILABLE);
        vehicle(m.get("T-Cross"), 2023, "Cinza", "134900.00", 22100, VehicleStatus.AVAILABLE);
        vehicle(m.get("Strada"), 2022, "Branco", "98700.00", 38900, VehicleStatus.SOLD);
        vehicle(m.get("Pulse"), 2024, "Azul", "109900.00", 9800, VehicleStatus.AVAILABLE);
        vehicle(m.get("Onix"), 2020, "Prata", "64900.00", 71500, VehicleStatus.AVAILABLE);
        vehicle(m.get("Onix"), 2023, "Preto", "82500.00", 15400, VehicleStatus.RESERVED);
        vehicle(m.get("Tracker"), 2022, "Branco", "119900.00", 33700, VehicleStatus.AVAILABLE);
        vehicle(m.get("HB20"), 2019, "Cinza", "58900.00", 88200, VehicleStatus.DISCONTINUED);
        vehicle(m.get("Creta"), 2024, "Verde", "154900.00", 4100, VehicleStatus.AVAILABLE);
        vehicle(m.get("Civic"), 2021, "Preto", "139900.00", 45600, VehicleStatus.SOLD);
        vehicle(m.get("HR-V"), 2023, "Branco", "162900.00", 12900, VehicleStatus.AVAILABLE);

        log.info("Banco populado com {} marcas, {} modelos e {} veículos de exemplo",
                brands.count(), models.count(), vehicles.count());
    }

    private VehicleModel model(String name, VehicleCategory category, Brand brand) {
        return models.save(new VehicleModel(name, category, brand));
    }

    private void vehicle(VehicleModel model, int year, String color, String price, int km, VehicleStatus status) {
        Vehicle v = new Vehicle(model, year, color, new BigDecimal(price), km);
        v.changeStatus(status);
        vehicles.save(v);
    }
}
