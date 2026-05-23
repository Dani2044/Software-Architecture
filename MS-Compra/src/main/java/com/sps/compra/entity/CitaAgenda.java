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
public class CitaAgenda {
    private String codigoServicio;
    private String nombreServicio;
    private Integer duracionMinutos;
}
