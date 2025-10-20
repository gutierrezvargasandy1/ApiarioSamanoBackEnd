package com.ApiarioSamano.MicroServiceAlmacen.controller;

import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.AlmacenDTO.AlmacenRequest;
import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import com.ApiarioSamano.MicroServiceAlmacen.services.AlmacenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/almacenes")
public class AlmacenController {

    @Autowired
    private AlmacenService almacenService;

    // Crear o actualizar un almacén
    @PostMapping("/crear")
    public CodigoResponse<Almacen> guardarAlmacen(@RequestBody AlmacenRequest request) {
        return almacenService.guardarAlmacen(request);
    }

    // Obtener todos los almacenes
    @GetMapping
    public CodigoResponse<List<Almacen>> obtenerTodos() {
        return almacenService.obtenerTodos();
    }

    // Obtener un almacén por ID
    @GetMapping("/{id}")
    public CodigoResponse<Almacen> obtenerPorId(@PathVariable Long id) {
        return almacenService.obtenerPorId(id);
    }

    // Eliminar un almacén por ID
    @DeleteMapping("/{id}")
    public CodigoResponse<Void> eliminarPorId(@PathVariable Long id) {
        return almacenService.eliminarPorId(id);
    }
}
