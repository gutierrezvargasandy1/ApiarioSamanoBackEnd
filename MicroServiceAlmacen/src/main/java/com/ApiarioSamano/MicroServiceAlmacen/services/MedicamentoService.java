package com.ApiarioSamano.MicroServiceAlmacen.services;

import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.model.Medicamento;
import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import com.ApiarioSamano.MicroServiceAlmacen.repository.MedicamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicamentoService {

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    // Crear o actualizar
    public CodigoResponse<Medicamento> guardar(Medicamento medicamento) {
        Medicamento guardado = medicamentoRepository.save(medicamento);
        return new CodigoResponse<>(200, "Medicamento guardado correctamente", guardado);
    }

    // Obtener todos
    public CodigoResponse<List<Medicamento>> obtenerTodos() {
        List<Medicamento> lista = medicamentoRepository.findAll();
        return new CodigoResponse<>(200, "Lista de medicamentos obtenida", lista);
    }

    // Obtener por ID
    public CodigoResponse<Medicamento> obtenerPorId(Long id) {
        Optional<Medicamento> opt = medicamentoRepository.findById(id);
        if (opt.isPresent()) {
            return new CodigoResponse<>(200, "Medicamento encontrado", opt.get());
        }
        return new CodigoResponse<>(404, "Medicamento no encontrado", null);
    }

    // Eliminar por ID
    public CodigoResponse<Void> eliminarPorId(Long id) {
        if (medicamentoRepository.existsById(id)) {
            medicamentoRepository.deleteById(id);
            return new CodigoResponse<>(200, "Medicamento eliminado correctamente", null);
        }
        return new CodigoResponse<>(404, "Medicamento no encontrado", null);
    }

    // Obtener por almacén
    public CodigoResponse<List<Medicamento>> obtenerPorAlmacen(Almacen almacen) {
        List<Medicamento> lista = medicamentoRepository.findByAlmacen(almacen);
        return new CodigoResponse<>(200, "Medicamentos del almacén obtenidos", lista);
    }
}
