package br.edu.unifecaf.automotivo.domain;

/** O registro pedido não existe. HTTP 404. */
public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " com id " + id + " não encontrado(a)");
    }
}
