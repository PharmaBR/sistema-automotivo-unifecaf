package br.edu.unifecaf.automotivo.web.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Envelope de paginação estável para o JSON (Generics: funciona para qualquer tipo T). */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
