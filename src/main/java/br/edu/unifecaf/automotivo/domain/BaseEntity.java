package br.edu.unifecaf.automotivo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

/**
 * HERANÇA: superclasse abstrata de todas as entidades.
 * Centraliza o identificador e os campos de auditoria (criado/atualizado em),
 * evitando repetir o mesmo código em Brand, VehicleModel e Vehicle.
 *
 * @MappedSuperclass = os campos daqui viram colunas nas tabelas das subclasses
 * (como um "abstract = True" num Model do Django).
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT do MySQL
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Callback do JPA executado antes do INSERT. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /** Callback do JPA executado antes do UPDATE. */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ENCAPSULAMENTO: só getters. O id e as datas são controlados pelo banco/JPA,
    // nunca alterados diretamente por quem usa o objeto.
    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
