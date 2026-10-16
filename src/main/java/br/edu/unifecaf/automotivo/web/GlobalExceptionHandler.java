package br.edu.unifecaf.automotivo.web;

import br.edu.unifecaf.automotivo.domain.BusinessRuleException;
import br.edu.unifecaf.automotivo.domain.ResourceNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converte exceções em respostas HTTP padronizadas (RFC 9457 "Problem Details").
 * Assim os controllers ficam limpos e todo erro tem o mesmo formato JSON:
 * { "status": 422, "title": "...", "detail": "..." }
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE) // tem prioridade sobre os tratadores padrão do Spring
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio violada", ex.getMessage());
    }

    /** Erros de @Valid no corpo (MethodArgumentNotValidException é subclasse) e de binding na query. */
    @ExceptionHandler(BindException.class)
    public ProblemDetail handleValidation(BindException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Um ou mais campos não passaram na validação");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ProblemDetail handleBadRequest(Exception ex) {
        String detail = (ex instanceof HttpMessageNotReadableException)
                ? "JSON inválido ou valor fora do esperado (verifique enums como status e category)"
                : ex.getMessage();
        return problem(HttpStatus.BAD_REQUEST, "Requisição inválida", detail);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleIntegrity(DataIntegrityViolationException ex) {
        return problem(HttpStatus.CONFLICT, "Conflito de dados",
                "A operação viola uma restrição do banco (registro duplicado ou em uso)");
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
