package com.ApiarioSamano.MicroServiceAlmacen.services;

import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.AlmacenDTO.AlmacenResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO.MateriasPrimasConProveedorDTO;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO.MateriasPrimasRequest;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO.MateriasPrimasResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.ProveedoresClientMicroserviceDTO.ProveedorResponseDTO;
import com.ApiarioSamano.MicroServiceAlmacen.model.MateriasPrimas;
import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import com.ApiarioSamano.MicroServiceAlmacen.repository.AlmacenRepository;
import com.ApiarioSamano.MicroServiceAlmacen.repository.MateriasPrimasRepository;
import com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI.ProveedoresClientMicroservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MateriasPrimasService {

    private final MateriasPrimasRepository materiasPrimasRepository;
    private final ProveedoresClientMicroservice proveedoresClient;
    private final AlmacenRepository almacenRepository;

    private AlmacenResponse mapAlmacen(Almacen almacen) {
        if (almacen == null)
            return null;

        AlmacenResponse response = new AlmacenResponse();
        response.setId(almacen.getId());
        response.setNumeroSeguimiento(almacen.getNumeroSeguimiento());
        response.setUbicacion(almacen.getUbicacion());
        response.setCapacidad(almacen.getCapacidad());

        return response;
    }

    private MateriasPrimasResponse mapMateria(MateriasPrimas m) {
        MateriasPrimasResponse response = new MateriasPrimasResponse();
        response.setId(m.getId());
        response.setNombre(m.getNombre());
        response.setFoto(m.getFoto());
        response.setCantidad(m.getCantidad());
        response.setIdProveedor(m.getIdProveedor());

        return response;
    }

    @Transactional
    public CodigoResponse<MateriasPrimasResponse> guardar(MateriasPrimasRequest req) {
        log.info("🔍 Iniciando proceso de guardar materia prima: {}", req.getNombre());

        log.info("🔍 Consultando microservicio de proveedores...");
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();
        log.info("✅ Proveedores obtenidos: {} registros", proveedores.size());

        boolean existeProveedor = proveedores.stream()
                .anyMatch(p -> p.getId().equals(req.getIdProvedor().longValue()));

        if (!existeProveedor) {
            log.warn("⚠️ Proveedor con ID {} no encontrado", req.getIdProvedor());
            return new CodigoResponse<>(404, "Proveedor no encontrado", null);
        }
        log.info("✅ Proveedor ID {} validado correctamente", req.getIdProvedor());

        log.info("🔍 Buscando almacén con ID: {}", req.getIdAlmacen());
        Almacen almacen = almacenRepository.findById(req.getIdAlmacen())
                .orElseThrow(() -> {
                    log.error("❌ Almacén con ID {} no encontrado", req.getIdAlmacen());
                    return new RuntimeException("Almacén no encontrado con ID: " + req.getIdAlmacen());
                });
        log.info("✅ Almacén encontrado: ID {}, Ubicación: {}", almacen.getId(), almacen.getUbicacion());

        // Verificar capacidad del almacén antes de agregar
        int espaciosOcupados = calcularEspaciosOcupados(almacen);
        log.info("📊 Capacidad del almacén: {}, Espacios ocupados: {}", almacen.getCapacidad(), espaciosOcupados);

        if (espaciosOcupados >= almacen.getCapacidad()) {
            log.error("❌ No hay capacidad disponible en el almacén. Capacidad: {}, Ocupados: {}",
                    almacen.getCapacidad(), espaciosOcupados);
            return new CodigoResponse<>(400, "No hay capacidad disponible en el almacén", null);
        }

        // Crear y guardar la materia prima
        MateriasPrimas materia = new MateriasPrimas();
        materia.setNombre(req.getNombre());
        materia.setFoto(req.getFoto());
        materia.setCantidad(req.getCantidad());
        materia.setAlmacen(almacen);
        materia.setIdProveedor(req.getIdProvedor());

        log.info("💾 Guardando materia prima en base de datos...");
        MateriasPrimas guardada = materiasPrimasRepository.save(materia);
        log.info("✅ Materia prima guardada exitosamente con ID: {}", guardada.getId());

        // Actualizar la lista de materias primas del almacén
        if (almacen.getMateriasPrimas() == null) {
            almacen.setMateriasPrimas(new java.util.ArrayList<>());
        }
        almacen.getMateriasPrimas().add(guardada);
        almacenRepository.save(almacen);
        log.info("✅ Materia prima agregada al almacén. Nuevos espacios ocupados: {}",
                calcularEspaciosOcupados(almacen));

        return new CodigoResponse<>(200, "Materia prima guardada correctamente", mapMateria(guardada));
    }

    // Método auxiliar para calcular espacios ocupados
    private int calcularEspaciosOcupados(Almacen almacen) {
        int espacios = 0;

        // Contar materias primas
        if (almacen.getMateriasPrimas() != null) {
            espacios += almacen.getMateriasPrimas().size();
        }

        // Contar herramientas (si existen en tu modelo)
        if (almacen.getHerramientas() != null) {
            espacios += almacen.getHerramientas().size();
        }

        // Contar medicamentos (si existen en tu modelo)
        if (almacen.getMedicamentos() != null) {
            espacios += almacen.getMedicamentos().size();
        }

        return espacios;
    }

    public CodigoResponse<List<MateriasPrimasResponse>> obtenerTodas() {
        log.info("📋 Obteniendo todas las materias primas de la base de datos");
        List<MateriasPrimasResponse> lista = materiasPrimasRepository.findAll()
                .stream()
                .map(this::mapMateria)
                .collect(Collectors.toList());
        log.info("✅ Se obtuvieron {} materias primas", lista.size());
        return new CodigoResponse<>(200, "Lista de materias primas obtenida", lista);
    }

    public CodigoResponse<MateriasPrimasResponse> obtenerPorId(Long id) {
        log.info("🔍 Buscando materia prima con ID: {}", id);
        Optional<MateriasPrimas> opt = materiasPrimasRepository.findById(id);

        if (opt.isPresent()) {
            log.info("✅ Materia prima encontrada: {}", opt.get().getNombre());
            return new CodigoResponse<>(200, "Materia prima encontrada", mapMateria(opt.get()));
        } else {
            log.warn("⚠️ Materia prima con ID {} no encontrada", id);
            return new CodigoResponse<>(404, "Materia prima no encontrada", null);
        }
    }

    @Transactional
    public CodigoResponse<Void> eliminarPorId(Long id) {
        log.info("🗑️ Intentando eliminar materia prima con ID: {}", id);
        Optional<MateriasPrimas> optMateria = materiasPrimasRepository.findById(id);

        if (optMateria.isPresent()) {
            MateriasPrimas materia = optMateria.get();
            Almacen almacen = materia.getAlmacen();

            // Eliminar la materia prima
            materiasPrimasRepository.deleteById(id);
            log.info("✅ Materia prima con ID {} eliminada correctamente", id);

            // Actualizar el almacén removiendo la materia prima
            if (almacen != null && almacen.getMateriasPrimas() != null) {
                almacen.getMateriasPrimas().removeIf(m -> m.getId().equals(id));
                almacenRepository.save(almacen);
                log.info("✅ Materia prima removida del almacén. Nuevos espacios ocupados: {}",
                        calcularEspaciosOcupados(almacen));
            }

            return new CodigoResponse<>(200, "Materia prima eliminada correctamente", null);
        }
        log.warn("⚠️ No se puede eliminar, materia prima con ID {} no encontrada", id);
        return new CodigoResponse<>(404, "Materia prima no encontrada", null);
    }

    // ================== MÉTODOS CON PROVEEDOR ==================
    public CodigoResponse<List<MateriasPrimasConProveedorDTO>> obtenerTodasConProveedor() {
        log.info("📋 Obteniendo materias primas con información de proveedor");

        List<MateriasPrimas> materias = materiasPrimasRepository.findAll();
        log.info("✅ Se obtuvieron {} materias primas", materias.size());

        log.info("🔍 Consultando microservicio de proveedores...");
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();
        log.info("✅ Se obtuvieron {} proveedores", proveedores.size());

        List<MateriasPrimasConProveedorDTO> resultado = materias.stream().map(m -> {
            MateriasPrimasConProveedorDTO dto = new MateriasPrimasConProveedorDTO();
            dto.setId(m.getId());
            dto.setNombre(m.getNombre());
            dto.setFoto(m.getFoto());
            dto.setCantidad(m.getCantidad());
            dto.setAlmacen(mapAlmacen(m.getAlmacen()));

            proveedores.stream()
                    .filter(p -> p.getId().equals(m.getIdProveedor().longValue()))
                    .findFirst()
                    .ifPresent(dto::setProveedor);

            return dto;
        }).collect(Collectors.toList());

        log.info("✅ Materias primas con proveedor mapeadas: {} registros", resultado.size());
        return new CodigoResponse<>(200, "Materias primas con proveedor obtenidas", resultado);
    }

    public CodigoResponse<MateriasPrimasConProveedorDTO> obtenerPorIdConProveedor(Long id) {
        log.info("🔍 Buscando materia prima con proveedor, ID: {}", id);

        Optional<MateriasPrimas> optMateria = materiasPrimasRepository.findById(id);
        if (optMateria.isEmpty()) {
            log.warn("⚠️ Materia prima con ID {} no encontrada", id);
            return new CodigoResponse<>(404, "Materia prima no encontrada", null);
        }

        MateriasPrimas materia = optMateria.get();
        log.info("✅ Materia prima encontrada: {}", materia.getNombre());

        log.info("🔍 Consultando microservicio de proveedores...");
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();

        MateriasPrimasConProveedorDTO dto = new MateriasPrimasConProveedorDTO();
        dto.setId(materia.getId());
        dto.setNombre(materia.getNombre());
        dto.setFoto(materia.getFoto());
        dto.setCantidad(materia.getCantidad());
        dto.setAlmacen(mapAlmacen(materia.getAlmacen()));

        proveedores.stream()
                .filter(p -> p.getId().equals(materia.getIdProveedor().longValue()))
                .findFirst()
                .ifPresent(proveedor -> {
                    dto.setProveedor(proveedor);
                    log.info("✅ Proveedor asociado: {}", proveedor.getNombreEmpresa());
                });

        return new CodigoResponse<>(200, "Materia prima con proveedor obtenida", dto);
    }

    public CodigoResponse<List<MateriasPrimasResponse>> obtenerPorAlmacen(Almacen almacen) {
        log.info("🔍 Buscando materias primas del almacén ID: {}", almacen.getId());
        List<MateriasPrimasResponse> lista = materiasPrimasRepository.findByAlmacen(almacen)
                .stream()
                .map(this::mapMateria)
                .collect(Collectors.toList());
        log.info("✅ Se encontraron {} materias primas en el almacén", lista.size());
        return new CodigoResponse<>(200, "Materias primas del almacén obtenidas", lista);
    }

    public CodigoResponse<List<MateriasPrimasResponse>> obtenerPorProveedor(Integer idProveedor) {
        log.info("🔍 Buscando materias primas del proveedor ID: {}", idProveedor);

        log.info("🔍 Validando existencia del proveedor...");
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();
        boolean existeProveedor = proveedores.stream()
                .anyMatch(p -> p.getId().equals(idProveedor.longValue()));

        if (!existeProveedor) {
            log.warn("⚠️ Proveedor con ID {} no encontrado", idProveedor);
            return new CodigoResponse<>(404, "Proveedor no encontrado", List.of());
        }
        log.info("✅ Proveedor validado correctamente");

        List<MateriasPrimasResponse> lista = materiasPrimasRepository.findByIdProveedor(idProveedor)
                .stream()
                .map(this::mapMateria)
                .collect(Collectors.toList());

        log.info("✅ Se encontraron {} materias primas del proveedor {}", lista.size(), idProveedor);
        return new CodigoResponse<>(200, "Materias primas del proveedor obtenidas", lista);
    }
}