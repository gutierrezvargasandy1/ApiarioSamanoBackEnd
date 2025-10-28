package com.ApiarioSamano.MicroServiceAlmacen.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ApiarioSamano.MicroServiceAlmacen.model.Medicamento;
import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import com.ApiarioSamano.MicroServiceAlmacen.repository.AlmacenRepository;
import com.ApiarioSamano.MicroServiceAlmacen.repository.MedicamentoRepository;
import com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI.ProveedoresClientMicroservice;
import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MedicamentosDTO.MedicamentosRequest;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MedicamentosDTO.MedicamentosResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MedicamentosDTO.MedicamnetosConProveedorResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.ProveedoresClientMicroserviceDTO.ProveedorResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicamentosService {

    private final MedicamentoRepository medicamentosRepository;
    private final AlmacenRepository almacenRepository;
    private final ProveedoresClientMicroservice proveedorClient;

    // 📌 Crear o actualizar medicamento
    @Transactional
    public MedicamentosResponse guardar(MedicamentosRequest request) {
        log.info("🔍 Iniciando proceso de guardar medicamento: {}", request.getNombre());

        log.info("🔍 Consultando microservicio de proveedores...");
        List<ProveedorResponseDTO> proveedores = proveedorClient.obtenerTodosProveedores();
        log.info("✅ Proveedores obtenidos: {} registros", proveedores.size());

        boolean existeProveedor = proveedores.stream()
                .anyMatch(p -> p.getId().equals(request.getIdProveedor().longValue()));

        if (!existeProveedor) {
            log.warn("⚠️ Proveedor con ID {} no encontrado", request.getIdProveedor());
            throw new RuntimeException("Proveedor no encontrado con ID: " + request.getIdProveedor());
        }
        log.info("✅ Proveedor ID {} validado correctamente", request.getIdProveedor());

        log.info("🔍 Buscando almacén con ID: {}", request.getIdAlmacen());
        Almacen almacen = almacenRepository.findById(request.getIdAlmacen().longValue())
                .orElseThrow(() -> {
                    log.error("❌ Almacén con ID {} no encontrado", request.getIdAlmacen());
                    return new RuntimeException("Almacén no encontrado con ID: " + request.getIdAlmacen());
                });
        log.info("✅ Almacén encontrado: ID {}, Ubicación: {}", almacen.getId(), almacen.getUbicacion());

        // Verificar capacidad del almacén antes de agregar
        int espaciosOcupados = calcularEspaciosOcupados(almacen);
        log.info("📊 Capacidad del almacén: {}, Espacios ocupados: {}", almacen.getCapacidad(), espaciosOcupados);

        if (espaciosOcupados >= almacen.getCapacidad()) {
            log.error("❌ No hay capacidad disponible en el almacén. Capacidad: {}, Ocupados: {}",
                    almacen.getCapacidad(), espaciosOcupados);
            throw new RuntimeException("No hay capacidad disponible en el almacén");
        }

        // Crear y guardar el medicamento
        Medicamento medicamento = new Medicamento();
        medicamento.setNombre(request.getNombre());
        medicamento.setDescripcion(request.getDescripcion());
        medicamento.setCantidad(request.getCantidad());
        medicamento.setFoto(request.getFoto());
        medicamento.setAlmacen(almacen);
        medicamento.setIdProveedor(request.getIdProveedor());

        log.info("💾 Guardando medicamento en base de datos...");
        Medicamento guardado = medicamentosRepository.save(medicamento);
        log.info("✅ Medicamento guardado exitosamente con ID: {}", guardado.getId());

        // Actualizar la lista de medicamentos del almacén
        if (almacen.getMedicamentos() == null) {
            almacen.setMedicamentos(new java.util.ArrayList<>());
        }
        almacen.getMedicamentos().add(guardado);
        almacenRepository.save(almacen);
        log.info("✅ Medicamento agregado al almacén. Nuevos espacios ocupados: {}", calcularEspaciosOcupados(almacen));

        return mapToResponse(guardado);
    }

    // Método auxiliar para calcular espacios ocupados
    private int calcularEspaciosOcupados(Almacen almacen) {
        int espacios = 0;

        // Contar medicamentos
        if (almacen.getMedicamentos() != null) {
            espacios += almacen.getMedicamentos().size();
        }

        // Contar materias primas (si existen en tu modelo)
        if (almacen.getMateriasPrimas() != null) {
            espacios += almacen.getMateriasPrimas().size();
        }

        // Contar herramientas (si existen en tu modelo)
        if (almacen.getHerramientas() != null) {
            espacios += almacen.getHerramientas().size();
        }

        return espacios;
    }

    // 📌 Obtener medicamento por ID
    public MedicamentosResponse obtenerPorId(Long id) {
        log.info("🔍 Buscando medicamento con ID: {}", id);
        Medicamento medicamento = medicamentosRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("⚠️ Medicamento con ID {} no encontrado", id);
                    return new RuntimeException("Medicamento no encontrado con ID: " + id);
                });

        log.info("✅ Medicamento encontrado: {}", medicamento.getNombre());
        return mapToResponse(medicamento);
    }

    // 📌 Obtener todos los medicamentos (sin proveedor)
    public List<MedicamentosResponse> obtenerTodos() {
        log.info("📋 Obteniendo todos los medicamentos de la base de datos");
        List<MedicamentosResponse> lista = medicamentosRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.info("✅ Se obtuvieron {} medicamentos", lista.size());
        return lista;
    }

    // 📌 Obtener todos los medicamentos con su proveedor (consulta al microservicio
    // Proveedores)
    public List<MedicamnetosConProveedorResponse> obtenerTodosConProveedor() {
        log.info("📋 Obteniendo medicamentos con información de proveedor");

        List<Medicamento> medicamentos = medicamentosRepository.findAll();
        log.info("✅ Se obtuvieron {} medicamentos", medicamentos.size());

        log.info("🔍 Consultando microservicio de proveedores...");
        List<ProveedorResponseDTO> proveedores = proveedorClient.obtenerTodosProveedores();
        log.info("✅ Se obtuvieron {} proveedores", proveedores.size());

        List<MedicamnetosConProveedorResponse> resultado = medicamentos.stream()
                .map(this::mapToResponseConProveedor)
                .collect(Collectors.toList());

        log.info("✅ Medicamentos con proveedor mapeados: {} registros", resultado.size());
        return resultado;
    }

    // 📌 Obtener medicamentos por ID de proveedor
    public List<MedicamentosResponse> obtenerPorProveedor(Integer idProveedor) {
        log.info("🔍 Buscando medicamentos del proveedor ID: {}", idProveedor);

        log.info("🔍 Validando existencia del proveedor...");
        List<ProveedorResponseDTO> proveedores = proveedorClient.obtenerTodosProveedores();
        boolean existeProveedor = proveedores.stream()
                .anyMatch(p -> p.getId().equals(idProveedor.longValue()));

        if (!existeProveedor) {
            log.warn("⚠️ Proveedor con ID {} no encontrado", idProveedor);
            throw new RuntimeException("Proveedor no encontrado con ID: " + idProveedor);
        }
        log.info("✅ Proveedor validado correctamente");

        List<MedicamentosResponse> lista = medicamentosRepository.findByIdProveedor(idProveedor)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.info("✅ Se encontraron {} medicamentos del proveedor {}", lista.size(), idProveedor);
        return lista;
    }

    // 📌 Obtener medicamentos por ID de proveedor (con datos del proveedor)
    public List<MedicamnetosConProveedorResponse> obtenerPorProveedorConDetalle(Integer idProveedor) {
        log.info("🔍 Buscando medicamentos del proveedor ID: {} con detalles", idProveedor);

        log.info("🔍 Validando existencia del proveedor...");
        List<ProveedorResponseDTO> proveedores = proveedorClient.obtenerTodosProveedores();
        boolean existeProveedor = proveedores.stream()
                .anyMatch(p -> p.getId().equals(idProveedor.longValue()));

        if (!existeProveedor) {
            log.warn("⚠️ Proveedor con ID {} no encontrado", idProveedor);
            throw new RuntimeException("Proveedor no encontrado con ID: " + idProveedor);
        }
        log.info("✅ Proveedor validado correctamente");

        List<MedicamnetosConProveedorResponse> lista = medicamentosRepository.findByIdProveedor(idProveedor)
                .stream()
                .map(this::mapToResponseConProveedor)
                .collect(Collectors.toList());

        log.info("✅ Se encontraron {} medicamentos del proveedor {} con detalles", lista.size(), idProveedor);
        return lista;
    }

    // 📌 Eliminar medicamento
    @Transactional
    public void eliminar(Long id) {
        log.info("🗑️ Intentando eliminar medicamento con ID: {}", id);
        Optional<Medicamento> optMedicamento = medicamentosRepository.findById(id);

        if (optMedicamento.isPresent()) {
            Medicamento medicamento = optMedicamento.get();
            Almacen almacen = medicamento.getAlmacen();

            // Eliminar el medicamento
            medicamentosRepository.deleteById(id);
            log.info("✅ Medicamento con ID {} eliminado correctamente", id);

            // Actualizar el almacén removiendo el medicamento
            if (almacen != null && almacen.getMedicamentos() != null) {
                almacen.getMedicamentos().removeIf(m -> m.getId().equals(id));
                almacenRepository.save(almacen);
                log.info("✅ Medicamento removido del almacén. Nuevos espacios ocupados: {}",
                        calcularEspaciosOcupados(almacen));
            }
        } else {
            log.warn("⚠️ No se puede eliminar, medicamento con ID {} no encontrado", id);
            throw new RuntimeException("Medicamento no encontrado con ID: " + id);
        }
    }

    // ==========================
    // MÉTODOS DE MAPEOS
    // ==========================
    private MedicamentosResponse mapToResponse(Medicamento m) {
        MedicamentosResponse response = new MedicamentosResponse();
        response.setId(m.getId());
        response.setNombre(m.getNombre());
        response.setDescripcion(m.getDescripcion());
        response.setCantidad(m.getCantidad());
        response.setFoto(m.getFoto());
        response.setIdProveedor(m.getIdProveedor());
        return response;
    }

    private MedicamnetosConProveedorResponse mapToResponseConProveedor(Medicamento m) {
        MedicamnetosConProveedorResponse response = new MedicamnetosConProveedorResponse();
        response.setId(m.getId());
        response.setNombre(m.getNombre());
        response.setDescripcion(m.getDescripcion());
        response.setCantidad(m.getCantidad());
        response.setFoto(m.getFoto());

        try {
            // Obtener todos los proveedores desde el microservicio
            List<ProveedorResponseDTO> proveedores = proveedorClient.obtenerTodosProveedores();

            // Buscar el proveedor correspondiente al idProveedor del medicamento
            ProveedorResponseDTO proveedor = proveedores.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(m.getIdProveedor().longValue()))
                    .findFirst()
                    .orElse(null);

            response.setProveedor(proveedor);
            if (proveedor != null) {
                log.debug("✅ Proveedor asociado al medicamento {}: {}", m.getId(), proveedor.getNombreEmpresa());
            }
        } catch (Exception e) {
            // En caso de error en la comunicación con el microservicio
            response.setProveedor(null);
            log.error("⚠️ Error al obtener proveedor para medicamento ID {}: {}", m.getId(), e.getMessage());
        }

        return response;
    }
}