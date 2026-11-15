package br.edu.unifecaf.automotivo.web;

import br.edu.unifecaf.automotivo.domain.BusinessRuleException;
import br.edu.unifecaf.automotivo.domain.ResourceNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
            // Falha de conversão (ex.: status=FOO): mensagem curta em vez do texto técnico do Spring
            String message = error.isBindingFailure()
                    ? "valor inválido: " + error.getRejectedValue()
                    : error.getDefaultMessage();
            errors.putIfAbsent(error.getField(), message);
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
        String detail;
        if (ex instanceof HttpMessageNotReadableException) {
            detail = "JSON inválido ou valor fora do esperado (verifique enums como status e category)";
        } else if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
            detail = "Valor inválido para '" + mismatch.getName() + "': " + mismatch.getValue();
        } else {
            detail = ex.getMessage();
        }
        return problem(HttpStatus.BAD_REQUEST, "Requisição inválida", detail);
    }

    // Erros do próprio Spring MVC também saem no formato Problem Details

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return problem(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido",
                "O método " + ex.getMethod() + " não é aceito nesta rota");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato não suportado",
                "Envie o corpo da requisição como application/json");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoRoute(NoResourceFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso não encontrado", "Rota inexistente: /" + ex.getResourcePath());
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
