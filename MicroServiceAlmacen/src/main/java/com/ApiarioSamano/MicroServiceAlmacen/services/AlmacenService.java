package com.ApiarioSamano.MicroServiceAlmacen.services;

import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.AlmacenDTO.*;
import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import com.ApiarioSamano.MicroServiceAlmacen.repository.AlmacenRepository;
import com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI.GeneradorCodigoClient;
import com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI.LotesClient;

import jakarta.transaction.Transactional;

import com.ApiarioSamano.MicroServiceAlmacen.dto.GeneradorCodigoClient.AlmacenRequestClient;
import com.ApiarioSamano.MicroServiceAlmacen.dto.HerramientasDTO.HerramientasResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.LotesClientMicroserviceDTO.LoteResponseDTO;
import com.ApiarioSamano.MicroServiceAlmacen.dto.LotesClientMicroserviceDTO.ReporteEspaciosResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO.MateriasPrimasResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MedicamentosDTO.MedicamentosResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AlmacenService {

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private GeneradorCodigoClient generadorCodigoClient;
    // Agrega estas dependencias en tu AlmacenService
    @Autowired
    private LotesClient lotesClient;

    /**
     * Calcula y actualiza los espacios ocupados para un almacén específico
     * Incluye: materias primas + herramientas + medicamentos + lotes externos
     */
    @Transactional
    public CodigoResponse<AlmacenResponse> actualizarEspaciosOcupadosAutomaticamente(Long idAlmacen) {
        try {
            log.info("🔄 Calculando espacios ocupados automáticamente para almacén ID: {}", idAlmacen);

            Optional<Almacen> opt = almacenRepository.findById(idAlmacen);
            if (opt.isEmpty()) {
                log.error("❌ Almacén con ID {} no encontrado", idAlmacen);
                return new CodigoResponse<>(404, "Almacén no encontrado", null);
            }

            Almacen almacen = opt.get();

            // 1. Calcular espacios de items internos
            int espaciosInternos = calcularEspaciosOcupadosInternos(almacen);
            log.info("📦 Espacios internos calculados: {}", espaciosInternos);

            // 2. Obtener y contar lotes externos que coincidan con este almacén
            int espaciosLotes = 0;
            try {
                List<LoteResponseDTO> lotes = lotesClient.obtenerTodosLotes();
                if (lotes != null) {
                    // Filtrar lotes que pertenecen a este almacén
                    espaciosLotes = (int) lotes.stream()
                            .filter(lote -> lote.getIdAlmacen() != null && lote.getIdAlmacen().equals(idAlmacen))
                            .count();
                    log.info("📋 Lotes externos encontrados para almacén {}: {}", idAlmacen, espaciosLotes);

                    // Log detallado de los lotes encontrados
                    if (espaciosLotes > 0) {
                        log.debug("📝 Detalle de lotes encontrados:");
                        lotes.stream()
                                .filter(lote -> lote.getIdAlmacen() != null && lote.getIdAlmacen().equals(idAlmacen))
                                .forEach(lote -> log.debug("   - Lote ID: {}, Código: {}, Producto: {}",
                                        lote.getId(), lote.getNumeroSeguimiento(), lote.getTipoProducto()));
                    }
                }
            } catch (Exception e) {
                log.warn("⚠️ No se pudieron obtener lotes externos: {}. Continuando solo con espacios internos",
                        e.getMessage());
                espaciosLotes = 0;
            }

            // 3. Calcular total
            int totalEspaciosOcupados = espaciosInternos + espaciosLotes;

            log.info("📊 Resumen final - Almacén ID: {}, Internos: {}, Lotes: {}, Total: {}, Capacidad: {}",
                    idAlmacen, espaciosInternos, espaciosLotes, totalEspaciosOcupados, almacen.getCapacidad());

            // 4. Validar capacidad
            if (totalEspaciosOcupados > almacen.getCapacidad()) {
                log.warn("🚨 Capacidad excedida - Total: {}, Capacidad: {}", totalEspaciosOcupados,
                        almacen.getCapacidad());
                // No bloqueamos, solo informamos
            }

            // 5. Actualizar el almacén (si tienes un campo para espacios ocupados)
            // almacen.setEspaciosOcupados(totalEspaciosOcupados);
            Almacen almacenActualizado = almacenRepository.save(almacen);

            AlmacenResponse response = mapToResponse(almacenActualizado);
            // Si tu AlmacenResponse tiene campos para espacios ocupados, setéalos aquí
            // response.setEspaciosOcupados(totalEspaciosOcupados);
            // response.setEspaciosDisponibles(almacen.getCapacidad() -
            // totalEspaciosOcupados);

            return new CodigoResponse<>(200,
                    String.format("Espacios ocupados actualizados. Internos: %d, Lotes: %d, Total: %d",
                            espaciosInternos, espaciosLotes, totalEspaciosOcupados),
                    response);

        } catch (Exception e) {
            log.error("❌ Error al actualizar espacios ocupados automáticamente: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al actualizar espacios ocupados: " + e.getMessage(), null);
        }
    }

    /**
     * Método auxiliar para calcular espacios ocupados por items internos
     */
    private int calcularEspaciosOcupadosInternos(Almacen almacen) {
        int espacios = 0;

        if (almacen.getMateriasPrimas() != null) {
            espacios += almacen.getMateriasPrimas().size();
        }

        if (almacen.getHerramientas() != null) {
            espacios += almacen.getHerramientas().size();
        }

        if (almacen.getMedicamentos() != null) {
            espacios += almacen.getMedicamentos().size();
        }

        return espacios;
    }

    /**
     * Método para obtener el reporte completo de espacios ocupados
     */
    public CodigoResponse<ReporteEspaciosResponse> obtenerReporteEspaciosOcupados(Long idAlmacen) {
        try {
            log.info("📋 Generando reporte de espacios ocupados para almacén ID: {}", idAlmacen);

            Optional<Almacen> opt = almacenRepository.findById(idAlmacen);
            if (opt.isEmpty()) {
                return new CodigoResponse<>(404, "Almacén no encontrado", null);
            }

            Almacen almacen = opt.get();

            // Calcular espacios internos
            int materiasPrimas = almacen.getMateriasPrimas() != null ? almacen.getMateriasPrimas().size() : 0;
            int herramientas = almacen.getHerramientas() != null ? almacen.getHerramientas().size() : 0;
            int medicamentos = almacen.getMedicamentos() != null ? almacen.getMedicamentos().size() : 0;
            int espaciosInternos = materiasPrimas + herramientas + medicamentos;

            // Obtener lotes externos
            int lotesExternos = 0;
            List<LoteResponseDTO> detalleLotes = new ArrayList<>();

            try {
                List<LoteResponseDTO> lotes = lotesClient.obtenerTodosLotes();
                if (lotes != null) {
                    detalleLotes = lotes.stream()
                            .filter(lote -> lote.getIdAlmacen() != null && lote.getIdAlmacen().equals(idAlmacen))
                            .collect(Collectors.toList());
                    lotesExternos = detalleLotes.size();
                }
            } catch (Exception e) {
                log.warn("⚠️ No se pudieron obtener lotes externos para el reporte: {}", e.getMessage());
            }

            int totalEspacios = espaciosInternos + lotesExternos;
            int espaciosDisponibles = Math.max(0, almacen.getCapacidad() - totalEspacios);

            // Crear respuesta del reporte
            ReporteEspaciosResponse reporte = new ReporteEspaciosResponse();
            reporte.setAlmacenId(idAlmacen);
            reporte.setCapacidadTotal(almacen.getCapacidad());
            reporte.setEspaciosInternos(espaciosInternos);
            reporte.setMateriasPrimas(materiasPrimas);
            reporte.setHerramientas(herramientas);
            reporte.setMedicamentos(medicamentos);
            reporte.setLotesExternos(lotesExternos);
            reporte.setTotalEspaciosOcupados(totalEspacios);
            reporte.setEspaciosDisponibles(espaciosDisponibles);
            reporte.setDetalleLotes(detalleLotes);
            reporte.setPorcentajeOcupacion((double) totalEspacios / almacen.getCapacidad() * 100);

            log.info("📊 Reporte generado - Total ocupados: {}/{}, Disponibles: {}",
                    totalEspacios, almacen.getCapacidad(), espaciosDisponibles);

            return new CodigoResponse<>(200, "Reporte de espacios generado correctamente", reporte);

        } catch (Exception e) {
            log.error("❌ Error al generar reporte de espacios: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al generar reporte: " + e.getMessage(), null);
        }
    }

    // Crear o actualizar
    public CodigoResponse<AlmacenResponse> guardarAlmacen(AlmacenRequest almacenRequest) {
        try {
            log.info("Solicitando código al microservicio de generador de códigos");

            String codigoResponse = generadorCodigoClient.generarAlmacen(
                    new AlmacenRequestClient(almacenRequest.getUbicacion(), "AlmacenGeneral"));
            log.info("Código generado recibido del microservicio: {}", codigoResponse);

            Almacen nuevoAlmacen = new Almacen();
            nuevoAlmacen.setNumeroSeguimiento(codigoResponse);
            nuevoAlmacen.setUbicacion(almacenRequest.getUbicacion());
            nuevoAlmacen.setCapacidad(almacenRequest.getCapacidad());

            // Inicializar las listas como vacías para evitar null
            nuevoAlmacen.setHerramientas(new ArrayList<>());
            nuevoAlmacen.setMateriasPrimas(new ArrayList<>());
            nuevoAlmacen.setMedicamentos(new ArrayList<>());

            Almacen almacenGuardado = almacenRepository.save(nuevoAlmacen);
            log.info("Almacén guardado correctamente con ID: {}", almacenGuardado.getId());

            return new CodigoResponse<>(200, "Almacén guardado correctamente", mapToResponse(almacenGuardado));

        } catch (Exception e) {
            log.error("Error al guardar el almacén: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al guardar el almacén: " + e.getMessage(), null);
        }
    }

    // Obtener todos
    public CodigoResponse<List<AlmacenResponse>> obtenerTodos() {
        List<Almacen> almacenes = almacenRepository.findAll();
        List<AlmacenResponse> lista = almacenes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return new CodigoResponse<>(200, "Lista de almacenes obtenida", lista);
    }

    // Obtener por ID
    public CodigoResponse<AlmacenResponse> obtenerPorId(Long id) {
        Optional<Almacen> opt = almacenRepository.findById(id);
        if (opt.isPresent()) {
            return new CodigoResponse<>(200, "Almacén encontrado", mapToResponse(opt.get()));
        }
        return new CodigoResponse<>(404, "Almacén no encontrado", null);
    }

    // Eliminar por ID
    public CodigoResponse<Void> eliminarPorId(Long id) {
        if (almacenRepository.existsById(id)) {
            almacenRepository.deleteById(id);
            return new CodigoResponse<>(200, "Almacén eliminado correctamente", null);
        }
        return new CodigoResponse<>(404, "Almacén no encontrado", null);
    }

    // Mapeo de entidad a Response (ÚNICO método mapToResponse)
    private AlmacenResponse mapToResponse(Almacen a) {
        AlmacenResponse response = new AlmacenResponse();
        response.setId(a.getId());
        response.setNumeroSeguimiento(a.getNumeroSeguimiento());
        response.setUbicacion(a.getUbicacion());
        response.setCapacidad(a.getCapacidad());

        // Materias primas - manejar lista nula
        List<MateriasPrimasResponse> materias = a.getMateriasPrimas() != null
                ? a.getMateriasPrimas().stream()
                        .map(m -> new MateriasPrimasResponse(m.getId(), m.getNombre(), m.getFoto(),
                                m.getCantidad(), m.getIdProveedor()))
                        .collect(Collectors.toList())
                : new ArrayList<>();
        response.setMateriasPrimas(materias);

        // Herramientas - manejar lista nula
        List<HerramientasResponse> herramientas = a.getHerramientas() != null
                ? a.getHerramientas().stream()
                        .map(h -> new HerramientasResponse(h.getId(), h.getNombre(),
                                h.getFoto(), h.getIdProveedor()))
                        .collect(Collectors.toList())
                : new ArrayList<>();
        response.setHerramientas(herramientas);

        // Medicamentos - manejar lista nula
        List<MedicamentosResponse> medicamentos = a.getMedicamentos() != null
                ? a.getMedicamentos().stream()
                        .map(m -> new MedicamentosResponse(m.getId(), m.getNombre(),
                                m.getCantidad(), m.getDescripcion(), m.getFoto(), m.getIdProveedor()))
                        .collect(Collectors.toList())
                : new ArrayList<>();
        response.setMedicamentos(medicamentos);

        return response;
    }
}