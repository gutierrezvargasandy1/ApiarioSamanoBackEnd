package com.ApiarioSamano.MicroServiceProduccion.repository;

import com.ApiarioSamano.MicroServiceProduccion.model.Cosecha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CosechaRepository extends JpaRepository<Cosecha, Long> {
    List<Cosecha> findByLoteId(Long idLote);
}
