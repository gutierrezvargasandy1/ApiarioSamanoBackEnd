package com.ApiarioSamano.MicroServiceAlmacen.services;

import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.model.MateriasPrimas;
import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import com.ApiarioSamano.MicroServiceAlmacen.repository.MateriasPrimasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MateriasPrimasService {

    @Autowired
    private MateriasPrimasRepository materiasPrimasRepository;

    // Crear o actualizar
    public CodigoResponse<MateriasPrimas> guardar(MateriasPrimas mp) {
        MateriasPrimas guardado = materiasPrimasRepository.save(mp);
        return new CodigoResponse<>(200, "Materia prima guardada correctamente", guardado);
    }

    // Obtener todas
    public CodigoResponse<List<MateriasPrimas>> obtenerTodas() {
        List<MateriasPrimas> lista = materiasPrimasRepository.findAll();
        return new CodigoResponse<>(200, "Lista de materias primas obtenida", lista);
    }

    // Obtener por ID
    public CodigoResponse<MateriasPrimas> obtenerPorId(Long id) {
        Optional<MateriasPrimas> opt = materiasPrimasRepository.findById(id);
        if (opt.isPresent()) {
            return new CodigoResponse<>(200, "Materia prima encontrada", opt.get());
        }
        return new CodigoResponse<>(404, "Materia prima no encontrada", null);
    }

    // Eliminar por ID
    public CodigoResponse<Void> eliminarPorId(Long id) {
        if (materiasPrimasRepository.existsById(id)) {
            materiasPrimasRepository.deleteById(id);
            return new CodigoResponse<>(200, "Materia prima eliminada correctamente", null);
        }
        return new CodigoResponse<>(404, "Materia prima no encontrada", null);
    }

    // Obtener por almacén
    public CodigoResponse<List<MateriasPrimas>> obtenerPorAlmacen(Almacen almacen) {
        List<MateriasPrimas> lista = materiasPrimasRepository.findByAlmacen(almacen);
        return new CodigoResponse<>(200, "Materias primas del almacén obtenidas", lista);
    }

    // Obtener por proveedor
    public CodigoResponse<List<MateriasPrimas>> obtenerPorProveedor(Integer idProveedor) {
        List<MateriasPrimas> lista = materiasPrimasRepository.findByIdProveedor(idProveedor);
        return new CodigoResponse<>(200, "Materias primas del proveedor obtenidas", lista);
    }
}