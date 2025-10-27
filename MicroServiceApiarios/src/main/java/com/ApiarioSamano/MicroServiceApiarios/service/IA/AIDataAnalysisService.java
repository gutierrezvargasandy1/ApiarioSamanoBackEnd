package com.ApiarioSamano.MicroServiceApiarios.service.IA;

import com.ApiarioSamano.MicroServiceApiarios.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceApiarios.model.Apiarios;
import com.ApiarioSamano.MicroServiceApiarios.model.HistorialMedico;
import com.ApiarioSamano.MicroServiceApiarios.model.Receta;
import com.ApiarioSamano.MicroServiceApiarios.model.RecetaMedicamento;
import com.ApiarioSamano.MicroServiceApiarios.service.ApiariosService;
import com.ApiarioSamano.MicroServiceApiarios.service.TemperaturaApi;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class AIDataAnalysisService {

    private final ApiariosService apiariosService;
    private final OllamaService ollamaService;
    private final TemperaturaApi temperaturaApia;

    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public CodigoResponse<Map<String, Object>> obtenerDatosEstadisticosApiarios() {
        long startTime = System.currentTimeMillis();

        try {
            log.info("📊 Iniciando análisis estadístico de ApiariosDB...");

            CodigoResponse<List<Apiarios>> apiariosResponse = apiariosService.obtenerTodos();
            List<Apiarios> apiarios = apiariosResponse.getData();

            if (apiarios == null || apiarios.isEmpty()) {
                return new CodigoResponse<>(204, "No hay apiarios registrados", null);
            }

            long total = apiarios.size();
            long conTratamiento = apiarios.stream().filter(a -> a.getReceta() != null).count();
            long sinTratamiento = total - conTratamiento;

            Map<String, Long> distribucionSalud = apiarios.stream()
                    .collect(Collectors.groupingBy(
                            a -> a.getSalud() != null ? a.getSalud() : "No especificada",
                            Collectors.counting()));

            // Estadísticas del historial médico
            Map<String, Object> estadisticasHistorial = obtenerEstadisticasHistorialMedico(apiarios);

            Map<String, Object> resumen = new HashMap<>();
            resumen.put("totalApiarios", total);
            resumen.put("conTratamiento", conTratamiento);
            resumen.put("sinTratamiento", sinTratamiento);
            resumen.put("distribucionSalud", distribucionSalud);
            resumen.put("estadisticasHistorial", estadisticasHistorial);

            String datosTexto = prepararDatosParaAnalisis(apiarios);

            String systemPrompt = """
                    Eres un experto en manejo de apiarios.
                    A partir de los datos proporcionados:
                    - Resume el estado general de los apiarios.
                    - Identifica posibles áreas de mejora.
                    - Propón 2 acciones clave para fortalecer la salud general.
                    - Considera el historial médico en tu análisis.
                    Responde en máximo 150 palabras con formato de puntos (•).
                    """;

            String analisisIA = ollamaService.generateAnalysis(systemPrompt, datosTexto, 125);

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("analisisIA", analisisIA);
            resultado.put("resumenEstadistico", resumen);
            resultado.put("modeloUsado", ollamaService.getModel());
            resultado.put("tiempoProcesamiento", (System.currentTimeMillis() - startTime) + "ms");

            return new CodigoResponse<>(200, "Análisis estadístico generado con éxito", resultado);

        } catch (Exception e) {
            log.error("💥 Error en análisis estadístico: {}", e.getMessage());
            return new CodigoResponse<>(500, "Error generando análisis: " + e.getMessage(), null);
        }
    }

    public CodigoResponse<Map<String, Object>> obtenerSugerenciasPorApiario(Long idApiario) {
        long startTime = System.currentTimeMillis();

        try {
            CodigoResponse<Apiarios> apiarioResponse = apiariosService.obtenerPorId(idApiario);
            Apiarios apiario = apiarioResponse.getData();

            if (apiario == null) {
                return new CodigoResponse<>(404, "Apiario no encontrado", null);
            }

            // Obtener historial médico usando el método corregido
            HistorialMedico historialMedico = obtenerHistorialMedicoSeguro(apiario);

            String infoApiario = prepararInfoApiarioIndividual(apiario, historialMedico);

            String systemPrompt = """
                    Eres un experto apícola.
                    Basándote en la información del apiario, su salud, tratamiento actual y historial médico completo:
                    - Da 3 recomendaciones prácticas inmediatas.
                    - Indica si el apiario requiere monitoreo o intervención.
                    - Menciona una sugerencia de mejora ambiental si aplica.
                    - Considera el historial médico previo para recomendaciones personalizadas.
                    Máximo 120 palabras, formato de viñetas (•).
                    """;

            String respuestaIA = ollamaService.generateAnalysis(systemPrompt, infoApiario, 125);

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("apiario", apiario.getNumeroApiario());
            resultado.put("ubicacion", apiario.getUbicacion());
            resultado.put("saludActual", apiario.getSalud());
            resultado.put("sugerenciasIA", respuestaIA);
            resultado.put("historialMedico", historialMedico);
            resultado.put("estadisticasApiario", obtenerEstadisticasIndividuales(apiario, historialMedico));
            resultado.put("modeloUsado", ollamaService.getModel());
            resultado.put("tiempoProcesamiento", (System.currentTimeMillis() - startTime) + "ms");

            return new CodigoResponse<>(200, "Sugerencias generadas exitosamente", resultado);

        } catch (Exception e) {
            log.error("💥 Error generando sugerencias: {}", e.getMessage());
            return new CodigoResponse<>(500, "Error generando sugerencias: " + e.getMessage(), null);
        }
    }

    public CodigoResponse<Map<String, Object>> obtenerPrediccionesDeCuidado() {
        long startTime = System.currentTimeMillis();

        try {
            // 1️⃣ Obtener lista de apiarios
            CodigoResponse<List<Apiarios>> apiariosResponse = apiariosService.obtenerTodos();
            List<Apiarios> apiarios = apiariosResponse.getData();

            if (apiarios == null || apiarios.isEmpty()) {
                return new CodigoResponse<>(204, "No hay apiarios registrados para análisis", null);
            }

            // 2️⃣ Preparar datos base de los apiarios
            String datos = prepararDatosParaAnalisis(apiarios);

            log.error("Datos para predicción: {}", datos);

            // 3️⃣ Agregar contexto de ubicación y clima actual
            String ubicacion = "Dolores Hidalgo, Guanajuato, México";
            double temperaturaActual = temperaturaApia.obtenerTemperaturaActualDolores();
            log.error("Datos para predicción: {}", temperaturaActual);
            String contextoClimatico = String.format(
                    "Los apiarios están ubicados en %s. " +
                            "La temperatura actual es de %.1f°C. " +
                            "Considera este clima en tus predicciones y recomendaciones.\n\n",
                    ubicacion, temperaturaActual);
            log.error("Datos para predicción: {}", contextoClimatico);

            // 4️⃣ Instrucción al modelo IA
            String systemPrompt = """
                    Actúa como un asistente predictivo de apicultura especializado en clima y salud de colmenas.
                    Analiza los datos de los apiarios considerando el contexto climático actual y el historial médico.
                    Predice los principales riesgos en los próximos 30 días, incluyendo:
                    - Problemas posibles (enfermedades, estrés térmico, baja producción, plagas)
                    - Nivel de probabilidad (Alta/Media/Baja)
                    - Medida preventiva recomendada
                    - Considera patrones del historial médico
                    Ajusta las recomendaciones a las condiciones ambientales locales.
                    Responde en máximo 200 palabras y rapido.
                    """;

            // 5️⃣ Combinar datos con contexto
            String entradaIA = contextoClimatico + datos;

            // 6️⃣ Ejecutar análisis en el modelo IA
            String prediccionIA = ollamaService.generateAnalysis(systemPrompt, entradaIA, 300);

            if (prediccionIA == null || prediccionIA.isBlank()) {
                return new CodigoResponse<>(500, "El análisis de IA no devolvió resultado", null);
            }

            // 7️⃣ Construir respuesta final
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("prediccionesIA", prediccionIA);
            resultado.put("apiariosAnalizados", apiarios.size());
            resultado.put("ubicacion", ubicacion);
            resultado.put("temperaturaActual", temperaturaActual + "°C");
            resultado.put("resumenHistorial", obtenerResumenHistorialGlobal(apiarios));
            resultado.put("modeloUsado", ollamaService.getModel());
            resultado.put("tiempoProcesamiento", (System.currentTimeMillis() - startTime) + "ms");

            return new CodigoResponse<>(200, "Predicciones generadas exitosamente", resultado);

        } catch (Exception e) {
            log.error("💥 Error generando predicciones de cuidado: ", e);
            return new CodigoResponse<>(500, "Error generando predicciones: " + e.getMessage(), null);
        }
    }

    /**
     * Método seguro para obtener historial médico sin errores de casteo
     */
    private HistorialMedico obtenerHistorialMedicoSeguro(Apiarios apiario) {
        try {
            if (apiario.getHistorialMedico() != null && apiario.getHistorialMedico().getId() != null) {
                CodigoResponse<?> historialResponse = apiariosService
                        .obtenerHistorialMedicoPorId(apiario.getHistorialMedico().getId());

                if (historialResponse != null && historialResponse.getData() != null) {
                    // Verificar si el dato es ya un HistorialMedico
                    if (historialResponse.getData() instanceof HistorialMedico) {
                        return (HistorialMedico) historialResponse.getData();
                    }
                    // Si es un Map, convertirlo a HistorialMedico
                    else if (historialResponse.getData() instanceof Map) {
                        Map<String, Object> historialMap = (Map<String, Object>) historialResponse.getData();
                        return convertirMapAHistorialMedico(historialMap);
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("⚠️ No se pudo obtener el historial médico para el apiario {}: {}", apiario.getId(),
                    e.getMessage());
            return null;
        }
    }

    /**
     * Convierte un Map a HistorialMedico
     */
    private HistorialMedico convertirMapAHistorialMedico(Map<String, Object> historialMap) {
        try {
            HistorialMedico historial = new HistorialMedico();

            if (historialMap.containsKey("id")) {
                historial.setId(Long.valueOf(historialMap.get("id").toString()));
            }
            if (historialMap.containsKey("notas")) {
                historial.setNotas(historialMap.get("notas").toString());
            }
            if (historialMap.containsKey("fechaAplicacion")) {
                // Aquí necesitarías convertir el String a LocalDateTime según tu formato
                // Por ahora lo dejamos como String o null
            }

            return historial;
        } catch (Exception e) {
            log.error("Error convirtiendo Map a HistorialMedico: {}", e.getMessage());
            return null;
        }
    }

    private String prepararDatosParaAnalisis(List<Apiarios> apiarios) {
        if (apiarios == null || apiarios.isEmpty()) {
            return "No hay datos de apiarios disponibles para análisis.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("TOTAL APIARIOS: ").append(apiarios.size()).append("\n\n");
        sb.append("DATOS DETALLADOS CON HISTORIAL MÉDICO:\n");

        for (Apiarios apiario : apiarios) {
            sb.append("------------------------------------------------------------\n");
            sb.append("Apiario #").append(apiario.getNumeroApiario()).append("\n");
            sb.append("Ubicación: ").append(apiario.getUbicacion()).append("\n");
            sb.append("Estado de salud: ").append(apiario.getSalud()).append("\n");

            // 🏥 Historial médico completo (usando método seguro)
            HistorialMedico historial = obtenerHistorialMedicoSeguro(apiario);
            if (historial != null) {
                sb.append("=== HISTORIAL MÉDICO ===\n");
                sb.append("ID Historial: ").append(historial.getId()).append("\n");
                sb.append("Notas: ").append(historial.getNotas()).append("\n");
                if (historial.getFechaAplicacion() != null) {
                    sb.append("Fecha de aplicación: ").append(historial.getFechaAplicacion()).append("\n");
                }
            } else {
                sb.append("Historial médico: No disponible\n");
            }

            // 💊 Receta médica actual
            if (apiario.getReceta() != null) {
                Receta receta = apiario.getReceta();
                sb.append("=== TRATAMIENTO ACTUAL ===\n");
                sb.append("Descripción: ").append(receta.getDescripcion()).append("\n");
                sb.append("Fecha de creación: ").append(receta.getFechaDeCreacion()).append("\n");

                // Medicamentos de la receta
                if (receta.getMedicamentos() != null && !receta.getMedicamentos().isEmpty()) {
                    sb.append("Medicamentos: ");
                    for (RecetaMedicamento medicamento : receta.getMedicamentos()) {
                        sb.append("ID Medicamento: ").append(medicamento.getIdMedicamento());
                        if (medicamento.getMedicamentoInfo() != null) {
                            sb.append(" (Info disponible)");
                        }
                        sb.append("; ");
                    }
                    sb.append("\n");
                }
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Prepara la información de un apiario individual con historial médico completo
     */
    private String prepararInfoApiarioIndividual(Apiarios apiario, HistorialMedico historialMedico) {
        if (apiario == null) {
            return "No se encontró información del apiario solicitado.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Apiario #").append(apiario.getNumeroApiario()).append("\n");
        sb.append("Ubicación: ").append(apiario.getUbicacion()).append("\n");
        sb.append("Estado de salud actual: ").append(apiario.getSalud()).append("\n");

        // Tratamiento actual
        if (apiario.getReceta() != null) {
            Receta receta = apiario.getReceta();
            sb.append("=== TRATAMIENTO ACTUAL ===\n");
            sb.append("Descripción: ").append(receta.getDescripcion()).append("\n");
            sb.append("Fecha de creación: ").append(receta.getFechaDeCreacion()).append("\n");

            // Medicamentos
            if (receta.getMedicamentos() != null && !receta.getMedicamentos().isEmpty()) {
                sb.append("Medicamentos recetados:\n");
                for (RecetaMedicamento medicamento : receta.getMedicamentos()) {
                    sb.append("  - ID Medicamento: ").append(medicamento.getIdMedicamento());
                    if (medicamento.getMedicamentoInfo() != null) {
                        sb.append(" (").append(medicamento.getMedicamentoInfo().getNombre()).append(")");
                    }
                    sb.append("\n");
                }
            }
        } else {
            sb.append("Tratamiento actual: No tiene tratamiento asignado\n");
        }

        // Historial médico completo
        if (historialMedico != null) {
            sb.append("\n=== HISTORIAL MÉDICO COMPLETO ===\n");
            sb.append("ID Historial: ").append(historialMedico.getId()).append("\n");
            sb.append("Notas médicas: ").append(historialMedico.getNotas()).append("\n");
            if (historialMedico.getFechaAplicacion() != null) {
                sb.append("Fecha de aplicación: ").append(historialMedico.getFechaAplicacion()).append("\n");
            }
        } else {
            sb.append("Historial médico: No disponible\n");
        }

        return sb.toString();
    }

    /**
     * Obtiene estadísticas del historial médico de todos los apiarios
     */
    private Map<String, Object> obtenerEstadisticasHistorialMedico(List<Apiarios> apiarios) {
        Map<String, Object> estadisticas = new HashMap<>();

        long conHistorial = apiarios.stream()
                .filter(a -> a.getHistorialMedico() != null)
                .count();

        long sinHistorial = apiarios.size() - conHistorial;

        // Contar apiarios con notas en el historial
        long conNotasMedicas = apiarios.stream()
                .filter(a -> a.getHistorialMedico() != null)
                .filter(a -> a.getHistorialMedico().getNotas() != null &&
                        !a.getHistorialMedico().getNotas().isEmpty())
                .count();

        estadisticas.put("totalConHistorial", conHistorial);
        estadisticas.put("totalSinHistorial", sinHistorial);
        estadisticas.put("porcentajeConHistorial", String.format("%.1f%%", (conHistorial * 100.0) / apiarios.size()));
        estadisticas.put("conNotasMedicas", conNotasMedicas);

        return estadisticas;
    }

    /**
     * Obtiene estadísticas individuales para un apiario específico
     */
    private Map<String, Object> obtenerEstadisticasIndividuales(Apiarios apiario, HistorialMedico historial) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("tieneHistorial", historial != null);
        stats.put("tieneTratamientoActual", apiario.getReceta() != null);

        if (historial != null) {
            stats.put("tieneNotasMedicas",
                    historial.getNotas() != null && !historial.getNotas().isEmpty());
            stats.put("fechaUltimoHistorial", historial.getFechaAplicacion());
        }

        if (apiario.getReceta() != null) {
            stats.put("cantidadMedicamentos",
                    apiario.getReceta().getMedicamentos() != null ? apiario.getReceta().getMedicamentos().size() : 0);
            stats.put("fechaCreacionReceta", apiario.getReceta().getFechaDeCreacion());
        }

        return stats;
    }

    /**
     * Obtiene un resumen global del historial médico
     */
    private Map<String, Object> obtenerResumenHistorialGlobal(List<Apiarios> apiarios) {
        Map<String, Object> resumen = new HashMap<>();

        List<Apiarios> apiariosConHistorial = apiarios.stream()
                .filter(a -> a.getHistorialMedico() != null)
                .collect(Collectors.toList());

        resumen.put("totalApiariosConHistorial", apiariosConHistorial.size());
        resumen.put("porcentajeConHistorial",
                String.format("%.1f%%", (apiariosConHistorial.size() * 100.0) / apiarios.size()));

        // Apiarios con tratamiento actual
        long conTratamientoActual = apiarios.stream()
                .filter(a -> a.getReceta() != null)
                .count();

        resumen.put("conTratamientoActual", conTratamientoActual);
        resumen.put("porcentajeConTratamiento",
                String.format("%.1f%%", (conTratamientoActual * 100.0) / apiarios.size()));

        return resumen;
    }
}