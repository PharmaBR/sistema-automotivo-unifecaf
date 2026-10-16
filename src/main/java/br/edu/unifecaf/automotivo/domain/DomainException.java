package br.edu.unifecaf.automotivo.domain;

/**
 * HERANÇA também nas exceções: toda falha de domínio estende esta classe abstrata.
 * O tratador global (GlobalExceptionHandler) converte cada subtipo em um status HTTP.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
