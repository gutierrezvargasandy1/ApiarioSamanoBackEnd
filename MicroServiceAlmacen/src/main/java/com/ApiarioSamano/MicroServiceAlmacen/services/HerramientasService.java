package com.ApiarioSamano.MicroServiceAlmacen.services;

import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.model.Herramientas;
import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import com.ApiarioSamano.MicroServiceAlmacen.repository.HerramientasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HerramientasService {

    @Autowired
    private HerramientasRepository herramientasRepository;

    // Crear o actualizar
    public CodigoResponse<Herramientas> guardar(Herramientas herramienta) {
        Herramientas guardado = herramientasRepository.save(herramienta);
        return new CodigoResponse<>(200, "Herramienta guardada correctamente", guardado);
    }

    // Obtener todas
    public CodigoResponse<List<Herramientas>> obtenerTodas() {
        List<Herramientas> lista = herramientasRepository.findAll();
        return new CodigoResponse<>(200, "Lista de herramientas obtenida", lista);
    }

    // Obtener por ID
    public CodigoResponse<Herramientas> obtenerPorId(Long id) {
        Optional<Herramientas> opt = herramientasRepository.findById(id);
        if (opt.isPresent()) {
            return new CodigoResponse<>(200, "Herramienta encontrada", opt.get());
        }
        return new CodigoResponse<>(404, "Herramienta no encontrada", null);
    }

    // Eliminar por ID
    public CodigoResponse<Void> eliminarPorId(Long id) {
        if (herramientasRepository.existsById(id)) {
            herramientasRepository.deleteById(id);
            return new CodigoResponse<>(200, "Herramienta eliminada correctamente", null);
        }
        return new CodigoResponse<>(404, "Herramienta no encontrada", null);
    }

    // Obtener por almacén
    public CodigoResponse<List<Herramientas>> obtenerPorAlmacen(Almacen almacen) {
        List<Herramientas> lista = herramientasRepository.findByAlmacen(almacen);
        return new CodigoResponse<>(200, "Herramientas del almacén obtenidas", lista);
    }
}
