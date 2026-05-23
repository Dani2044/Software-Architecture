package com.sps.compra.repository;

import com.sps.compra.entity.Compra;
import com.sps.compra.entity.EstadoCompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepoCompra extends JpaRepository<Compra, Long> {
    List<Compra> findByEstado(EstadoCompra estado);
    List<Compra> findByCedulaCliente(String cedulaCliente);
}
