package com.sps.compra.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compra")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String cedulaCliente;

    @Column(length = 200)
    private String nombreCliente;

    @Column(length = 200)
    private String correoCliente;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EstadoCompra estado;

    @Column(length = 500)
    private String observacionSns;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<PlanSalud> planes = new ArrayList<>();

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (estado == null) estado = EstadoCompra.CREADA;
    }

    @PreUpdate
    void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
