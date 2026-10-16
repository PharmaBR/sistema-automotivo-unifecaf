package br.edu.unifecaf.automotivo.domain;

/** Uma regra de negócio foi violada (ex.: preço negativo, quilometragem diminuindo). HTTP 422. */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
