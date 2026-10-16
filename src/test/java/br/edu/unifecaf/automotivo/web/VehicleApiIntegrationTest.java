package br.edu.unifecaf.automotivo.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de INTEGRAÇÃO: sobem a aplicação inteira (controllers + serviços + JPA + H2)
 * e exercitam a API por HTTP simulado (MockMvc), como o APIClient do DRF.
 * @Transactional desfaz tudo ao fim de cada teste, mantendo-os independentes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("API de veículos")
class VehicleApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    private long toyotaId;
    private long corollaId;
    private long hiluxId;

    @BeforeEach
    void setUp() throws Exception {
        toyotaId = postAndGetId("/api/brands", """
                {"name": "Toyota", "country": "Japão"}""");
        corollaId = postAndGetId("/api/models", """
                {"name": "Corolla", "category": "SEDAN", "brandId": %d}""".formatted(toyotaId));
        hiluxId = postAndGetId("/api/models", """
                {"name": "Hilux", "category": "PICKUP", "brandId": %d}""".formatted(toyotaId));
        long fiatId = postAndGetId("/api/brands", """
                {"name": "Fiat", "country": "Itália"}""");
        long argoId = postAndGetId("/api/models", """
                {"name": "Argo", "category": "HATCH", "brandId": %d}""".formatted(fiatId));

        createVehicle(corollaId, 2022, "Prata", "120000.00", 30000);
        createVehicle(hiluxId, 2023, "Preto", "250000.00", 15000);
        createVehicle(argoId, 2021, "Branco", "70000.00", 45000);
    }

    @Test
    @DisplayName("POST cadastra veículo como Disponível e devolve 201 + Location")
    void createsVehicle() throws Exception {
        mvc.perform(post("/api/vehicles").contentType(MediaType.APPLICATION_JSON).content("""
                        {"modelId": %d, "manufactureYear": 2024, "color": "Azul",
                         "price": 155000.00, "mileage": 0}""".formatted(corollaId)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Corolla"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.statusLabel").value("Disponível"));
    }

    @Test
    @DisplayName("POST com dados inválidos devolve 400 com os campos problemáticos")
    void rejectsInvalidPayload() throws Exception {
        mvc.perform(post("/api/vehicles").contentType(MediaType.APPLICATION_JSON).content("""
                        {"modelId": %d, "manufactureYear": 2024, "color": "", "price": -5, "mileage": -1}"""
                        .formatted(corollaId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.color").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.mileage").exists());
    }

    @Test
    @DisplayName("GET filtra por marca, faixa de preço e ordena por preço")
    void filtersVehicles() throws Exception {
        mvc.perform(get("/api/vehicles").param("brandId", String.valueOf(toyotaId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mvc.perform(get("/api/vehicles")
                        .param("minPrice", "60000").param("maxPrice", "130000")
                        .param("sortBy", "price").param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].model").value("Corolla"))
                .andExpect(jsonPath("$.content[1].model").value("Argo"));

        mvc.perform(get("/api/vehicles").param("category", "PICKUP").param("minYear", "2023"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].model").value("Hilux"));

        mvc.perform(get("/api/vehicles").param("brand", "fia"))
                .andExpect(jsonPath("$.content[0].brand").value("Fiat"));
    }

    @Test
    @DisplayName("GET com filtro incoerente devolve 400")
    void rejectsIncoherentFilter() throws Exception {
        mvc.perform(get("/api/vehicles").param("minPrice", "100").param("maxPrice", "50"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/vehicles").param("sortBy", "senha"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH atualiza preço, quilometragem e status")
    void updatesVehicle() throws Exception {
        long id = createVehicle(corollaId, 2020, "Cinza", "90000.00", 60000);

        mvc.perform(patch("/api/vehicles/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"price": 85000.00, "mileage": 61000, "status": "RESERVED"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(85000.0))
                .andExpect(jsonPath("$.mileage").value(61000))
                .andExpect(jsonPath("$.status").value("RESERVED"));
    }

    @Test
    @DisplayName("PATCH que diminui a quilometragem devolve 422")
    void rejectsMileageDecrease() throws Exception {
        long id = createVehicle(corollaId, 2020, "Cinza", "90000.00", 60000);

        mvc.perform(patch("/api/vehicles/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"mileage": 1000}"""))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Regra de negócio violada"));
    }

    @Test
    @DisplayName("DELETE só remove veículo vendido ou descontinuado")
    void deleteRequiresSoldOrDiscontinued() throws Exception {
        long id = createVehicle(corollaId, 2020, "Cinza", "90000.00", 60000);

        mvc.perform(delete("/api/vehicles/{id}", id))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(patch("/api/vehicles/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"status": "SOLD"}"""))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/vehicles/{id}", id))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/vehicles/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("não remove marca que ainda tem modelos")
    void brandWithModelsCannotBeDeleted() throws Exception {
        mvc.perform(delete("/api/brands/{id}", toyotaId))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("não permite marca duplicada")
    void rejectsDuplicatedBrand() throws Exception {
        mvc.perform(post("/api/brands").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "toyota"}"""))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("summary conta veículos por status")
    void summarizesByStatus() throws Exception {
        mvc.perform(get("/api/vehicles/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.AVAILABLE").value(3))
                .andExpect(jsonPath("$.SOLD").value(0));
    }

    // ------------------------------------------------------------------ helpers

    private long createVehicle(long modelId, int year, String color, String price, int km) throws Exception {
        return postAndGetId("/api/vehicles", """
                {"modelId": %d, "manufactureYear": %d, "color": "%s", "price": %s, "mileage": %d}"""
                .formatted(modelId, year, color, price, km));
    }

    private long postAndGetId(String url, String json) throws Exception {
        MvcResult result = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn();
        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }
}
