package com.sps.compra.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compra_plan")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanSalud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    @JsonBackReference
    private Compra compra;

    @Column(nullable = false, length = 64)
    private String codigoPlan;

    @Column(nullable = false, length = 200)
    private String nombrePlan;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private ValidacionSNS estadoSns;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "compra_plan_servicio", joinColumns = @JoinColumn(name = "compra_plan_id"))
    @Builder.Default
    private List<ServicioMedico> servicios = new ArrayList<>();
}
