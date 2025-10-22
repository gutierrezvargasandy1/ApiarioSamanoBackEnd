package com.ApiarioSamano.MicroServiceProduccion.controller;

import com.ApiarioSamano.MicroServiceProduccion.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceProduccion.dto.CosechaDTO.CosechaRequest;
import com.ApiarioSamano.MicroServiceProduccion.model.Cosecha;
import com.ApiarioSamano.MicroServiceProduccion.services.CosechaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cosechas")
@RequiredArgsConstructor
public class CosechaController {

    private final CosechaService cosechaService;

    // 🔹 Crear o actualizar una cosecha
    @PostMapping("/crear")
    public CodigoResponse<Cosecha> guardarCosecha(@RequestBody CosechaRequest request) {
        return cosechaService.guardarCosecha(request);
    }

    // 🔹 Listar todas las cosechas
    @GetMapping
    public CodigoResponse<List<Cosecha>> listarCosechas() {
        return cosechaService.listarCosechas();
    }

    // 🔹 Obtener una cosecha por ID
    @GetMapping("/{id}")
    public CodigoResponse<Cosecha> obtenerCosechaPorId(@PathVariable Long id) {
        return cosechaService.obtenerPorId(id);
    }

    // 🔹 Eliminar una cosecha por ID
    @DeleteMapping("/{id}")
    public CodigoResponse<Void> eliminarCosecha(@PathVariable Long id) {
        return cosechaService.eliminarCosecha(id);
    }
}
