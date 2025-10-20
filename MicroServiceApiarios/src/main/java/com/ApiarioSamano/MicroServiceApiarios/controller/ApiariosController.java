package com.ApiarioSamano.MicroServiceApiarios.controller;

import com.ApiarioSamano.MicroServiceApiarios.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.ApiariosDTO.ApiarioRequestDTO;
import com.ApiarioSamano.MicroServiceApiarios.model.Receta;
import com.ApiarioSamano.MicroServiceApiarios.service.ApiariosService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/apiarios")
@RequiredArgsConstructor
public class ApiariosController {

    private final ApiariosService apiariosService;

    // 🟢 Crear un nuevo apiario
    @PostMapping("/nuevo")
    public CodigoResponse crearApiario(@RequestBody ApiarioRequestDTO apiarioDTO) {
        return apiariosService.crearApiario(apiarioDTO);
    }

    // 🟡 Modificar apiario existente
    @PutMapping("/modificar/{id}")
    public CodigoResponse modificarApiario(@PathVariable Long id, @RequestBody ApiarioRequestDTO apiario) {
        return apiariosService.modificarApiario(id, apiario);
    }

    // 🔴 Eliminar apiario (sin eliminar historial médico)
    @DeleteMapping("/eliminar/{id}")
    public CodigoResponse eliminarApiario(@PathVariable Long id) {
        return apiariosService.eliminarApiario(id);
    }

    // 🧾 Agregar receta a un apiario existente
    @PostMapping("/{idApiario}/agregar-receta")
    public CodigoResponse agregarReceta(@PathVariable Long idApiario, @RequestBody Receta receta) {
        return apiariosService.agregarReceta(idApiario, receta);
    }

    // 🧹 Eliminar receta cumplida de un apiario
    @DeleteMapping("/{idApiario}/eliminar-receta")
    public CodigoResponse eliminarRecetaCumplida(@PathVariable Long idApiario) {
        return apiariosService.eliminarRecetaCumplida(idApiario);
    }

    @GetMapping
    public CodigoResponse obtenerTodos() {
        return apiariosService.obtenerTodos();
    }

    // 🔍 Obtener apiario por ID
    @GetMapping("/{id}")
    public CodigoResponse obtenerPorId(@PathVariable Long id) {
        return apiariosService.obtenerPorId(id);
    }
}
