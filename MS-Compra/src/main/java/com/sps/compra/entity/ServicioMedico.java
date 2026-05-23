package com.sps.compra.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioMedico {
    private String codigo;
    private String nombre;
    private Integer duracionMinutos;
}
