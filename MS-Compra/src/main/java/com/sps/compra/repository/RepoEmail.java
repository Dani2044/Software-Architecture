package com.sps.compra.repository;

import com.sps.compra.entity.RegistroEmail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepoEmail extends JpaRepository<RegistroEmail, Long> {
}
