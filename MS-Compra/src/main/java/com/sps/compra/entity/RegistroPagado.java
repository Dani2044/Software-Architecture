package com.sps.compra.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "registro_pagado")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroPagado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long numeroCompra;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valorPagado;

    private LocalDateTime fechaPago;
}
