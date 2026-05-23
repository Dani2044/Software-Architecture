package com.sps.compra.controller;

import com.sps.compra.dto.CrearCompraRequest;
import com.sps.compra.entity.Compra;
import com.sps.compra.repository.RepoCompra;
import com.sps.compra.service.SrvCompras;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/compra")
@RequiredArgsConstructor
public class WSCompra {

    private final SrvCompras srvCompras;
    private final RepoCompra repoCompra;

    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@Valid @RequestBody CrearCompraRequest req) {
        Compra c = srvCompras.crearCompra(req);
        // 202 ACCEPTED: el cliente no espera respuesta inmediata (PDF/DOCX seccion 1.c)
        return ResponseEntity.accepted().body(Map.of(
                "numeroCompra", c.getId(),
                "estado", c.getEstado(),
                "mensaje", "Compra creada. Se enviara correo cuando la SNS valide los planes."
        ));
    }

    @GetMapping("/{id}")
    public Compra detalle(@PathVariable Long id) {
        return repoCompra.findById(id).orElseThrow();
    }

    @GetMapping("/cedula/{cedula}")
    public List<Compra> porCedula(@PathVariable String cedula) {
        return repoCompra.findByCedulaCliente(cedula);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "MS-Compra");
    }
}
