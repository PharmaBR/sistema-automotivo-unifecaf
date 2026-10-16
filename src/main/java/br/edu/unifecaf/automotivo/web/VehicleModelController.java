package br.edu.unifecaf.automotivo.web;

import br.edu.unifecaf.automotivo.service.VehicleModelService;
import br.edu.unifecaf.automotivo.web.dto.VehicleModelRequest;
import br.edu.unifecaf.automotivo.web.dto.VehicleModelResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/** Controller REST de modelos, sempre vinculados a uma marca (RF02). */
@RestController
@RequestMapping("/api/models")
@Tag(name = "Modelos", description = "Cadastro de modelos vinculados às marcas")
public class VehicleModelController {

    private final VehicleModelService service;

    public VehicleModelController(VehicleModelService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista modelos; use ?brandId= para filtrar por marca")
    public List<VehicleModelResponse> list(@RequestParam(required = false) Long brandId) {
        return service.findAll(brandId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um modelo pelo id")
    public VehicleModelResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra um modelo para uma marca")
    public ResponseEntity<VehicleModelResponse> create(@Valid @RequestBody VehicleModelRequest request) {
        VehicleModelResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/models/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um modelo")
    public VehicleModelResponse update(@PathVariable Long id, @Valid @RequestBody VehicleModelRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um modelo sem veículos vinculados")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
