package com.ApiarioSamano.MicroServiceApiarios.service;

import com.ApiarioSamano.MicroServiceApiarios.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.ApiariosDTO.ApiarioRequestDTO;
import com.ApiarioSamano.MicroServiceApiarios.dto.RecetaDTO.RecetaRequest;
import com.ApiarioSamano.MicroServiceApiarios.model.*;
import com.ApiarioSamano.MicroServiceApiarios.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class ApiariosService {

    private final ApiariosRepository apiariosRepository;
    private final RecetaRepository recetaRepository;
    private final HistorialMedicoRepository historialMedicoRepository;
    private final HistorialRecetasRepository historialRecetasRepository;

    // 🟢 Crear nuevo apiario (usando DTO)
    public CodigoResponse crearApiario(ApiarioRequestDTO apiarioDTO) {
        Apiarios apiario = new Apiarios();

        apiario.setNumeroApiario(apiarioDTO.getNumeroApiario());
        apiario.setUbicacion(apiarioDTO.getUbicacion());
        apiario.setSalud(apiarioDTO.getSalud());

        // Crear historial médico inicial si no existe
        HistorialMedico historial = new HistorialMedico();
        historial.setNotas("Historial inicial del apiario.");
        log.info("✅ [CREAR APIARIO] Entidad lista para guardar: {}", historial);
        historialMedicoRepository.save(historial);
        apiario.setHistorialMedico(historial);
        apiario.setReceta(null);

        log.info("✅ [CREAR APIARIO] Entidad lista para guardar: {}", apiario);

        apiariosRepository.save(apiario);

        return new CodigoResponse<>(200, "Apiario creado exitosamente", apiario);
    }

    // 🟡 Modificar un apiario existente
    public CodigoResponse modificarApiario(Long id, ApiarioRequestDTO datosActualizados) {
        Apiarios apiario = apiariosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Apiario no encontrado con ID: " + id));

        apiario.setNumeroApiario(datosActualizados.getNumeroApiario());
        apiario.setUbicacion(datosActualizados.getUbicacion());
        apiario.setSalud(datosActualizados.getSalud());

        apiariosRepository.save(apiario);

        return new CodigoResponse<>(200, "Apiario actualizado correctamente", apiario);
    }

    // 🔴 Eliminar apiario sin afectar historial médico
    public CodigoResponse eliminarApiario(Long id) {
        Apiarios apiario = apiariosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Apiario no encontrado con ID: " + id));

        // Desvincular el historial antes de eliminar
        apiario.setHistorialMedico(null);
        apiariosRepository.delete(apiario);

        return new CodigoResponse<>(200, "Apiario eliminado correctamente (historial conservado)", null);
    }

    // 🧾 Agregar receta a apiario existente usando DTO
    public CodigoResponse agregarReceta(Long idApiario, RecetaRequest recetaDTO) {
        Apiarios apiario = apiariosRepository.findById(idApiario)
                .orElseThrow(() -> new RuntimeException("Apiario no encontrado con ID: " + idApiario));

        // Crear entidad Receta a partir del DTO
        Receta receta = new Receta();
        receta.setDescripcion(recetaDTO.getDescripcion());

        // Guardar primero la receta para generar ID
        Receta recetaGuardada = recetaRepository.save(receta);

        // Si vienen medicamentos, asociarlos
        if (recetaDTO.getMedicamentos() != null && !recetaDTO.getMedicamentos().isEmpty()) {
            List<RecetaMedicamento> listaMedicamentos = recetaDTO.getMedicamentos().stream().map(medDTO -> {
                RecetaMedicamento rm = new RecetaMedicamento();
                rm.setReceta(recetaGuardada);
                rm.setMedicamento(medicoRepository.findById(medDTO.getId())
                        .orElseThrow(
                                () -> new RuntimeException("Medicamento no encontrado con ID: " + medDTO.getId())));
                return rm;
            }).toList();

            recetaGuardada.setMedicamentos(listaMedicamentos);
            recetaRepository.save(recetaGuardada);
        }

        // Asociar receta al apiario
        apiario.setReceta(recetaGuardada);

        // Asegurar que el apiario tiene historial médico
        HistorialMedico historial = apiario.getHistorialMedico();
        if (historial == null) {
            historial = new HistorialMedico();
            historial.setNotas("Historial creado automáticamente al asignar receta.");
            historialMedicoRepository.save(historial);
            apiario.setHistorialMedico(historial);
        }

        // Registrar receta en historial de recetas
        HistorialRecetas registro = new HistorialRecetas();
        registro.setHistorialMedico(historial);
        registro.setReceta(recetaGuardada);
        historialRecetasRepository.save(registro);

        // Guardar cambios en el apiario
        apiariosRepository.save(apiario);

        return new CodigoResponse<>(200, "Receta agregada exitosamente al apiario", apiario);
    }

    // 🧹 Eliminar receta cumplida del apiario
    public CodigoResponse eliminarRecetaCumplida(Long idApiario) {
        Apiarios apiario = apiariosRepository.findById(idApiario)
                .orElseThrow(() -> new RuntimeException("Apiario no encontrado con ID: " + idApiario));

        Receta receta = apiario.getReceta();
        if (receta == null) {
            throw new RuntimeException("El apiario no tiene receta asignada.");
        }

        // Eliminar de historial y BD
        historialRecetasRepository.deleteByReceta(receta);
        apiario.setReceta(null);
        apiariosRepository.save(apiario);
        recetaRepository.delete(receta);

        return new CodigoResponse<>(200, "Receta eliminada correctamente del apiario", apiario);
    }

    // 🔍 Obtener todos los apiarios
    public CodigoResponse obtenerTodos() {
        List<Apiarios> apiarios = apiariosRepository.findAll();
        return new CodigoResponse<>(200, "Apiarios obtenidos exitosamente", apiarios);
    }

    // 🔍 Obtener apiario por ID
    public CodigoResponse obtenerPorId(Long id) {
        Optional<Apiarios> apiario = apiariosRepository.findById(id);
        return new CodigoResponse<>(200, "Apiario obtenido exitosamente", apiario);
    }
}
