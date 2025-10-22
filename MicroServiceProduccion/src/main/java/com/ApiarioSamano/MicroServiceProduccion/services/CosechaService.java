package com.ApiarioSamano.MicroServiceProduccion.services;

import com.ApiarioSamano.MicroServiceProduccion.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceProduccion.dto.CosechaDTO.CosechaRequest;
import com.ApiarioSamano.MicroServiceProduccion.model.Cosecha;
import com.ApiarioSamano.MicroServiceProduccion.model.Lote;
import com.ApiarioSamano.MicroServiceProduccion.repository.CosechaRepository;
import com.ApiarioSamano.MicroServiceProduccion.repository.LoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CosechaService {

    @Autowired
    private CosechaRepository cosechaRepository;

    @Autowired
    private LoteRepository loteRepository;

    public CodigoResponse<Cosecha> guardarCosecha(CosechaRequest request) {
        try {
            Optional<Lote> loteOpt = loteRepository.findById(request.getIdLote().longValue());
            if (loteOpt.isEmpty()) {
                return new CodigoResponse<>(404, "Lote no encontrado", null);
            }

            Cosecha cosecha = new Cosecha();
            cosecha.setLote(loteOpt.get());
            cosecha.setCalidad(request.getCalidad());
            cosecha.setCantidad(request.getCantidad());
            cosecha.setIdApiario(request.getIdApiario().longValue());
            cosecha.setFechaCosecha(LocalDate.now());

            Cosecha guardada = cosechaRepository.save(cosecha);
            return new CodigoResponse<>(200, "Cosecha guardada correctamente", guardada);
        } catch (Exception e) {
            return new CodigoResponse<>(500, "Error al guardar la cosecha: " + e.getMessage(), null);
        }
    }

    // ✅ Obtener todas
    public CodigoResponse<List<Cosecha>> listarCosechas() {
        List<Cosecha> lista = cosechaRepository.findAll();
        return new CodigoResponse<>(200, "Listado de cosechas obtenido correctamente", lista);
    }

    // ✅ Buscar por ID
    public CodigoResponse<Cosecha> obtenerPorId(Long id) {
        return cosechaRepository.findById(id)
                .map(c -> new CodigoResponse<>(200, "Cosecha encontrada", c))
                .orElse(new CodigoResponse<>(404, "Cosecha no encontrada", null));
    }

    // ✅ Eliminar
    public CodigoResponse<Void> eliminarCosecha(Long id) {
        if (!cosechaRepository.existsById(id)) {
            return new CodigoResponse<>(404, "Cosecha no encontrada", null);
        }
        cosechaRepository.deleteById(id);
        return new CodigoResponse<>(200, "Cosecha eliminada correctamente", null);
    }
}
