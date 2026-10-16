package br.edu.unifecaf.automotivo.web;

import br.edu.unifecaf.automotivo.domain.VehicleStatus;
import br.edu.unifecaf.automotivo.repository.VehicleFilter;
import br.edu.unifecaf.automotivo.service.VehicleService;
import br.edu.unifecaf.automotivo.web.dto.PageResponse;
import br.edu.unifecaf.automotivo.web.dto.VehicleCreateRequest;
import br.edu.unifecaf.automotivo.web.dto.VehicleResponse;
import br.edu.unifecaf.automotivo.web.dto.VehicleUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * Controller REST do estoque de veículos.
 *
 *   GET    /api/vehicles          consulta com filtros (RF03)
 *   GET    /api/vehicles/{id}     detalhe
 *   GET    /api/vehicles/summary  total por status
 *   POST   /api/vehicles          cadastro (RF01)
 *   PATCH  /api/vehicles/{id}     atualização de preço/km/status/cor (RF04)
 *   DELETE /api/vehicles/{id}     remoção de vendidos/descontinuados (RF05)
 */
@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Veículos", description = "Estoque de veículos: cadastro, consulta, atualização e remoção")
public class VehicleController {

    private static final Set<String> SORTABLE_FIELDS =
            Set.of("id", "price", "manufactureYear", "mileage", "color", "status", "createdAt");
    private static final int MAX_PAGE_SIZE = 100;

    private final VehicleService service;

    public VehicleController(VehicleService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Consulta veículos com filtros combináveis",
            description = "Filtros: brandId, modelId, brand (nome), model (nome), category, status, "
                    + "minPrice, maxPrice, minYear, maxYear, color. Paginação: page, size. "
                    + "Ordenação: sortBy (price, manufactureYear, mileage...) e direction (asc/desc).")
    public PageResponse<VehicleResponse> search(
            @ParameterObject VehicleFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return service.search(filter, buildPageable(page, size, sortBy, direction));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um veículo pelo id")
    public VehicleResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/summary")
    @Operation(summary = "Quantidade de veículos por status (visão do gerente)")
    public Map<VehicleStatus, Long> summary() {
        return service.countByStatus();
    }

    @PostMapping
    @Operation(summary = "Cadastra um veículo no estoque (entra como Disponível)")
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleCreateRequest request) {
        VehicleResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/vehicles/" + created.id())).body(created);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualiza preço, quilometragem, status e/ou cor")
    public VehicleResponse update(@PathVariable Long id, @Valid @RequestBody VehicleUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um veículo vendido ou descontinuado")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Lista branca de campos ordenáveis: evita erro 500 com ?sortBy=campoInexistente. */
    private static Pageable buildPageable(int page, int size, String sortBy, String direction) {
        if (!SORTABLE_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("sortBy inválido. Use um destes: " + SORTABLE_FIELDS);
        }
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("page deve ser >= 0 e size entre 1 e " + MAX_PAGE_SIZE);
        }
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(dir, sortBy));
    }
}
