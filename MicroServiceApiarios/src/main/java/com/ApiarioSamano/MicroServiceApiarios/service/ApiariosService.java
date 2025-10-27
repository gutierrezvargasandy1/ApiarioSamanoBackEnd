package com.ApiarioSamano.MicroServiceApiarios.service;

import com.ApiarioSamano.MicroServiceApiarios.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.ApiariosDTO.ApiarioRequestDTO;
import com.ApiarioSamano.MicroServiceApiarios.dto.MedicamentosDTO.MedicamentosResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.RecetaDTO.RecetaRequest;
import com.ApiarioSamano.MicroServiceApiarios.model.*;
import com.ApiarioSamano.MicroServiceApiarios.repository.*;
import com.ApiarioSamano.MicroServiceApiarios.service.MicroServicesAPI.MicroServiceClientMedicamentos;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class ApiariosService {

    private final ApiariosRepository apiariosRepository;
    private final RecetaRepository recetaRepository;
    private final HistorialMedicoRepository historialMedicoRepository;
    private final HistorialRecetasRepository historialRecetasRepository;
    private final RecetaMedicamentoRepository recetaMedicamentoRepository;
    @Autowired
    private MicroServiceClientMedicamentos microServiceClientMedicamentos;

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

    public CodigoResponse<Receta> agregarReceta(Long idApiario, RecetaRequest recetaDTO) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Iniciando proceso para agregar receta al apiario ID: {}", idApiario);

        // Buscar apiario
        Apiarios apiario = apiariosRepository.findById(idApiario)
                .orElseThrow(() -> new RuntimeException("Apiario no encontrado con ID: " + idApiario));
        logger.info("Apiario encontrado: {}", apiario.getNumeroApiario());

        // Crear entidad Receta a partir del DTO
        Receta receta = new Receta();
        receta.setDescripcion(recetaDTO.getDescripcion());

        // ✅ INICIALIZAR LA LISTA DE MEDICAMENTOS
        receta.setMedicamentos(new ArrayList<>());

        Receta recetaGuardada = recetaRepository.save(receta);
        logger.info("Receta guardada con ID: {}", recetaGuardada.getId());

        // Asociar medicamentos si vienen en el DTO
        if (recetaDTO.getMedicamentos() != null && !recetaDTO.getMedicamentos().isEmpty()) {
            logger.info("Asociando medicamentos a la receta...");

            List<RecetaMedicamento> listaMedicamentos = recetaDTO.getMedicamentos().stream().map(medDTO -> {
                // ✅ USAR EL NUEVO MÉTODO obtenerPorId (MÁS EFICIENTE)
                MedicamentosResponse med = microServiceClientMedicamentos.obtenerPorId(medDTO.getId());

                RecetaMedicamento rm = new RecetaMedicamento();
                rm.setReceta(recetaGuardada);
                rm.setIdMedicamento(med.getId());
                rm.setMedicamentoInfo(med); // ✅ CARGAR INFO COMPLETA
                logger.info("Medicamento asociado a receta: {}", med.getNombre());

                return rm;
            }).toList();

            // ✅ GUARDAR MEDICAMENTOS Y ASOCIARLOS A LA RECETA
            List<RecetaMedicamento> medicamentosGuardados = recetaMedicamentoRepository.saveAll(listaMedicamentos);

            // ✅ ASOCIAR LA LISTA A LA RECETA (IMPORTANTE)
            recetaGuardada.setMedicamentos(medicamentosGuardados);
            recetaRepository.save(recetaGuardada); // ✅ ACTUALIZAR RECETA CON MEDICAMENTOS

            logger.info("{} medicamentos guardados y asociados a la receta", medicamentosGuardados.size());
        }

        // Asociar receta al historial médico del apiario
        logger.info("INICIANDO HISTORIAL MEDICO");
        HistorialMedico historial = apiario.getHistorialMedico();
        if (historial == null) {
            logger.info("CREANDO HISTORIAL MEDICO PORQUE NO TENIA");
            historial = new HistorialMedico();
            historial.setNotas("Historial creado automáticamente al asignar receta.");
            historialMedicoRepository.save(historial);
            apiario.setHistorialMedico(historial);

            // ✅ GUARDAR APIARIO CON NUEVO HISTORIAL
            apiariosRepository.save(apiario);
            logger.info("Historial médico creado automáticamente para el apiario.");
        }

        // Guardar relación en historialrecetas
        HistorialRecetas hr = new HistorialRecetas();
        hr.setHistorialMedico(historial);
        hr.setReceta(recetaGuardada);
        historialRecetasRepository.save(hr);
        logger.info("Receta agregada al historial del apiario mediante historialrecetas.");

        // Asociar receta al apiario
        apiario.setReceta(recetaGuardada);
        apiariosRepository.save(apiario);
        logger.info("Receta asociada al apiario y cambios guardados.");

        // ✅ CARGAR RECETA COMPLETA CON MEDICAMENTOS (PARA EVITAR PROBLEMAS DE CACHE)
        Receta recetaCompleta = recetaRepository.findById(recetaGuardada.getId())
                .orElseThrow(() -> new RuntimeException("Error al cargar receta completa"));

        return new CodigoResponse<>(200, "Receta agregada correctamente", recetaCompleta);
    }

    // 🔍 Obtener historial médico por ID con recetas y medicamentos completos
    public CodigoResponse obtenerHistorialMedicoPorId(Long idHistorial) {
        HistorialMedico historial = historialMedicoRepository.findById(idHistorial)
                .orElseThrow(() -> new RuntimeException("Historial médico no encontrado con ID: " + idHistorial));

        // Obtener todas las recetas asociadas a este historial
        List<HistorialRecetas> historialRecetas = historialRecetasRepository.findByHistorialMedico(historial);

        // Cargar información completa de cada receta con sus medicamentos
        List<Receta> recetasCompletas = new ArrayList<>();

        for (HistorialRecetas hr : historialRecetas) {
            Receta receta = hr.getReceta();

            // Cargar información de medicamentos para esta receta
            if (receta.getMedicamentos() != null) {
                for (RecetaMedicamento rm : receta.getMedicamentos()) {
                    try {
                        MedicamentosResponse medicamentoInfo = microServiceClientMedicamentos
                                .obtenerPorId(rm.getIdMedicamento());
                        rm.setMedicamentoInfo(medicamentoInfo);
                    } catch (Exception e) {
                        log.warn("No se pudo cargar información del medicamento ID: {} para receta ID: {}",
                                rm.getIdMedicamento(), receta.getId());
                        rm.setMedicamentoInfo(null);
                    }
                }
            }
            recetasCompletas.add(receta);
        }

        // Crear un DTO o mapa con la información completa
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("historialMedico", historial);
        respuesta.put("recetas", recetasCompletas);
        respuesta.put("totalRecetas", recetasCompletas.size());

        return new CodigoResponse<>(200, "Historial médico obtenido exitosamente", respuesta);
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

    // 🔍 Obtener apiario por ID con información completa de medicamentos
    public CodigoResponse obtenerPorId(Long id) {
        Apiarios apiario = apiariosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Apiario no encontrado con ID: " + id));

        // Cargar información de medicamentos si tiene receta
        if (apiario.getReceta() != null && apiario.getReceta().getMedicamentos() != null) {
            for (RecetaMedicamento rm : apiario.getReceta().getMedicamentos()) {
                try {
                    MedicamentosResponse medicamentoInfo = microServiceClientMedicamentos
                            .obtenerPorId(rm.getIdMedicamento());
                    rm.setMedicamentoInfo(medicamentoInfo);
                } catch (Exception e) {
                    log.warn("No se pudo cargar información del medicamento ID: {}", rm.getIdMedicamento());
                    rm.setMedicamentoInfo(null);
                }
            }
        }

        return new CodigoResponse<>(200, "Apiario obtenido exitosamente", apiario);
    }
}
