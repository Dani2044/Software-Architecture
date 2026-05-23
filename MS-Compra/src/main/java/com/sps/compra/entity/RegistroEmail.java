package com.sps.compra.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_email")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long numeroCompra;

    @Column(nullable = false, length = 64)
    private String tipo;

    @Column(nullable = false, length = 200)
    private String destinatario;

    private LocalDateTime fechaEnvio;

    @Column(length = 32)
    private String estado;
}
