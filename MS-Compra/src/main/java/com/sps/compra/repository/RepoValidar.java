package com.sps.compra.repository;

import com.sps.compra.entity.RegistroValidacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepoValidar extends JpaRepository<RegistroValidacion, Long> {
}
