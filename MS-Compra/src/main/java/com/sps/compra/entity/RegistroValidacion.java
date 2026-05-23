package com.sps.compra.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_validacion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroValidacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long numeroCompra;

    @Column(nullable = false, length = 32)
    private String cedula;

    @Column(length = 32)
    private String resultado;

    private LocalDateTime fechaValidacion;
}
