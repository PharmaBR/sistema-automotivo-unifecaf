package br.edu.unifecaf.automotivo.web;

import br.edu.unifecaf.automotivo.service.BrandService;
import br.edu.unifecaf.automotivo.web.dto.BrandRequest;
import br.edu.unifecaf.automotivo.web.dto.BrandResponse;
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
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Controller REST de marcas (RF02). Só traduz HTTP <-> serviço; nenhuma regra de negócio aqui.
 */
@RestController
@RequestMapping("/api/brands")
@Tag(name = "Marcas", description = "Cadastro de marcas de veículos")
public class BrandController {

    private final BrandService service;

    public BrandController(BrandService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista todas as marcas (ordem alfabética)")
    public List<BrandResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma marca pelo id")
    public BrandResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra uma marca")
    public ResponseEntity<BrandResponse> create(@Valid @RequestBody BrandRequest request) {
        BrandResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/brands/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma marca")
    public BrandResponse update(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma marca sem modelos vinculados")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
