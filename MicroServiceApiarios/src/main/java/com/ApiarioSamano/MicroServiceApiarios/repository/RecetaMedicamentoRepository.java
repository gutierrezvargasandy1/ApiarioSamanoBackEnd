package com.ApiarioSamano.MicroServiceApiarios.repository;

import com.ApiarioSamano.MicroServiceApiarios.model.RecetaMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecetaMedicamentoRepository extends JpaRepository<RecetaMedicamento, Long> {

}