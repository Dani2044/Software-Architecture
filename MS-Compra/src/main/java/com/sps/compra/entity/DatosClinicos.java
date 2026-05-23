package com.sps.compra.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatosClinicos {
    private String cedula;
    private String nombre;
    private String correo;
    private LocalDate fechaNacimiento;
}
