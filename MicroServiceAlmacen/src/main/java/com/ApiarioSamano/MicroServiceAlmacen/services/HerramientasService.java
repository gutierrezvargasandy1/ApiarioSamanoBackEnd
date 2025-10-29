package com.ApiarioSamano.MicroServiceAlmacen.services;

import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.HerramientasDTO.HerramientasConProveedorResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.HerramientasDTO.HerramientasRequest;
import com.ApiarioSamano.MicroServiceAlmacen.dto.HerramientasDTO.HerramientasResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.ProveedoresClientMicroserviceDTO.ProveedorResponseDTO;
import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import com.ApiarioSamano.MicroServiceAlmacen.model.Herramientas;
import com.ApiarioSamano.MicroServiceAlmacen.repository.AlmacenRepository;
import com.ApiarioSamano.MicroServiceAlmacen.repository.HerramientasRepository;
import com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI.ProveedoresClientMicroservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HerramientasService {

    private final HerramientasRepository herramientasRepository;
    private final AlmacenRepository almacenRepository;
    private final ProveedoresClientMicroservice proveedoresClient;

    // ================== Mapeos ==================
    private HerramientasResponse mapHerramienta(Herramientas h) {
        HerramientasResponse response = new HerramientasResponse();
        response.setId(h.getId());
        response.setNombre(h.getNombre());

        // Convertir byte[] a Base64 String
        if (h.getFoto() != null && h.getFoto().length > 0) {
            String fotoBase64 = Base64.getEncoder().encodeToString(h.getFoto());
            response.setFoto(fotoBase64);
        } else {
            response.setFoto(null);
        }

        response.setIdProveedor(h.getIdProveedor());
        return response;
    }

    // ================== CRUD ==================
    @Transactional
    public CodigoResponse<HerramientasResponse> guardar(HerramientasRequest req) {
        log.info("🔍 Iniciando proceso de guardar herramienta: {}", req.getNombre());

        log.info("🔍 Consultando microservicio de proveedores...");
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();
        log.info("✅ Proveedores obtenidos: {} registros", proveedores.size());

        boolean existeProveedor = proveedores.stream()
                .anyMatch(p -> p.getId().equals(req.getIdProveedor().longValue()));

        if (!existeProveedor) {
            log.warn("⚠️ Proveedor con ID {} no encontrado", req.getIdProveedor());
            return new CodigoResponse<>(404, "Proveedor no encontrado", null);
        }
        log.info("✅ Proveedor ID {} validado correctamente", req.getIdProveedor());

        log.info("🔍 Buscando almacén con ID: {}", req.getIdAlmacen());
        Almacen almacen = almacenRepository.findById(req.getIdAlmacen().longValue())
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

        // Crear y guardar la herramienta
        Herramientas herramienta = new Herramientas();
        herramienta.setNombre(req.getNombre());

        // Convertir Base64 String a byte[]
        if (req.getFoto() != null && !req.getFoto().isEmpty()) {
            try {
                byte[] fotoBytes = Base64.getDecoder().decode(req.getFoto());
                herramienta.setFoto(fotoBytes);
                log.info("✅ Foto convertida de Base64 a byte[], tamaño: {} bytes", fotoBytes.length);
            } catch (IllegalArgumentException e) {
                log.error("❌ Error al decodificar Base64 de la foto: {}", e.getMessage());
                return new CodigoResponse<>(400, "Formato Base64 de la foto inválido", null);
            }
        } else {
            herramienta.setFoto(null);
            log.info("ℹ️ No se proporcionó foto para la herramienta");
        }

        herramienta.setAlmacen(almacen);
        herramienta.setIdProveedor(req.getIdProveedor());

        log.info("💾 Guardando herramienta en base de datos...");
        Herramientas guardada = herramientasRepository.save(herramienta);
        log.info("✅ Herramienta guardada exitosamente con ID: {}", guardada.getId());

        // Actualizar la lista de herramientas del almacén
        if (almacen.getHerramientas() == null) {
            almacen.setHerramientas(new java.util.ArrayList<>());
        }
        almacen.getHerramientas().add(guardada);
        almacenRepository.save(almacen);
        log.info("✅ Herramienta agregada al almacén. Nuevos espacios ocupados: {}", calcularEspaciosOcupados(almacen));

        return new CodigoResponse<>(200, "Herramienta guardada correctamente", mapHerramienta(guardada));
    }

    // Método auxiliar para calcular espacios ocupados
    private int calcularEspaciosOcupados(Almacen almacen) {
        int espacios = 0;

        // Contar herramientas
        if (almacen.getHerramientas() != null) {
            espacios += almacen.getHerramientas().size();
        }

        // Contar materias primas (si existen en tu modelo)
        if (almacen.getMateriasPrimas() != null) {
            espacios += almacen.getMateriasPrimas().size();
        }

        // Contar medicamentos (si existen en tu modelo)
        if (almacen.getMedicamentos() != null) {
            espacios += almacen.getMedicamentos().size();
        }

        return espacios;
    }

    public CodigoResponse<List<HerramientasResponse>> obtenerTodas() {
        log.info("📋 Obteniendo todas las herramientas de la base de datos");
        List<HerramientasResponse> lista = herramientasRepository.findAll()
                .stream()
                .map(this::mapHerramienta)
                .collect(Collectors.toList());
        log.info("✅ Se obtuvieron {} herramientas", lista.size());
        return new CodigoResponse<>(200, "Lista de herramientas obtenida", lista);
    }

    public CodigoResponse<HerramientasResponse> obtenerPorId(Long id) {
        log.info("🔍 Buscando herramienta con ID: {}", id);
        Optional<Herramientas> opt = herramientasRepository.findById(id);

        if (opt.isPresent()) {
            log.info("✅ Herramienta encontrada: {}", opt.get().getNombre());
            return new CodigoResponse<>(200, "Herramienta encontrada", mapHerramienta(opt.get()));
        } else {
            log.warn("⚠️ Herramienta con ID {} no encontrada", id);
            return new CodigoResponse<>(404, "Herramienta no encontrada", null);
        }
    }

    @Transactional
    public CodigoResponse<Void> eliminar(Long id) {
        log.info("🗑️ Intentando eliminar herramienta con ID: {}", id);
        Optional<Herramientas> optHerramienta = herramientasRepository.findById(id);

        if (optHerramienta.isPresent()) {
            Herramientas herramienta = optHerramienta.get();
            Almacen almacen = herramienta.getAlmacen();

            // Eliminar la herramienta
            herramientasRepository.deleteById(id);
            log.info("✅ Herramienta con ID {} eliminada correctamente", id);

            // Actualizar el almacén removiendo la herramienta
            if (almacen != null && almacen.getHerramientas() != null) {
                almacen.getHerramientas().removeIf(h -> h.getId().equals(id));
                almacenRepository.save(almacen);
                log.info("✅ Herramienta removida del almacén. Nuevos espacios ocupados: {}",
                        calcularEspaciosOcupados(almacen));
            }

            return new CodigoResponse<>(200, "Herramienta eliminada correctamente", null);
        }
        log.warn("⚠️ No se puede eliminar, herramienta con ID {} no encontrada", id);
        return new CodigoResponse<>(404, "Herramienta no encontrada", null);
    }

    // ================== MÉTODOS CON PROVEEDOR ==================
    public CodigoResponse<List<HerramientasConProveedorResponse>> obtenerTodasConProveedor() {
        log.info("📋 Obteniendo herramientas con información de proveedor");

        List<Herramientas> herramientas = herramientasRepository.findAll();
        log.info("✅ Se obtuvieron {} herramientas", herramientas.size());

        log.info("🔍 Consultando microservicio de proveedores...");
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();
        log.info("✅ Se obtuvieron {} proveedores", proveedores.size());

        List<HerramientasConProveedorResponse> resultado = herramientas.stream().map(h -> {
            HerramientasConProveedorResponse dto = new HerramientasConProveedorResponse();
            dto.setId(h.getId());
            dto.setNombre(h.getNombre());

            // Convertir byte[] a Base64 String
            if (h.getFoto() != null && h.getFoto().length > 0) {
                String fotoBase64 = Base64.getEncoder().encodeToString(h.getFoto());
                dto.setFoto(fotoBase64);
            } else {
                dto.setFoto(null);
            }

            proveedores.stream()
                    .filter(p -> p.getId().equals(h.getIdProveedor().longValue()))
                    .findFirst()
                    .ifPresent(dto::setProveedor);

            return dto;
        }).collect(Collectors.toList());

        log.info("✅ Herramientas con proveedor mapeadas: {} registros", resultado.size());
        return new CodigoResponse<>(200, "Herramientas con proveedor obtenidas", resultado);
    }

    public CodigoResponse<HerramientasConProveedorResponse> obtenerPorIdConProveedor(Long id) {
        log.info("🔍 Buscando herramienta con proveedor, ID: {}", id);

        Optional<Herramientas> optHerramienta = herramientasRepository.findById(id);
        if (optHerramienta.isEmpty()) {
            log.warn("⚠️ Herramienta con ID {} no encontrada", id);
            return new CodigoResponse<>(404, "Herramienta no encontrada", null);
        }

        Herramientas herramienta = optHerramienta.get();
        log.info("✅ Herramienta encontrada: {}", herramienta.getNombre());

        log.info("🔍 Consultando microservicio de proveedores...");
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();

        HerramientasConProveedorResponse dto = new HerramientasConProveedorResponse();
        dto.setId(herramienta.getId());
        dto.setNombre(herramienta.getNombre());

        // Convertir byte[] a Base64 String
        if (herramienta.getFoto() != null && herramienta.getFoto().length > 0) {
            String fotoBase64 = Base64.getEncoder().encodeToString(herramienta.getFoto());
            dto.setFoto(fotoBase64);
        } else {
            dto.setFoto(null);
        }

        proveedores.stream()
                .filter(p -> p.getId().equals(herramienta.getIdProveedor().longValue()))
                .findFirst()
                .ifPresent(proveedor -> {
                    dto.setProveedor(proveedor);
                    log.info("✅ Proveedor asociado: {}", proveedor.getNombreEmpresa());
                });

        return new CodigoResponse<>(200, "Herramienta con proveedor obtenida", dto);
    }

    public CodigoResponse<List<HerramientasResponse>> obtenerPorProveedor(Integer idProveedor) {
        log.info("🔍 Buscando herramientas del proveedor ID: {}", idProveedor);

        log.info("🔍 Validando existencia del proveedor...");
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();
        boolean existeProveedor = proveedores.stream()
                .anyMatch(p -> p.getId().equals(idProveedor.longValue()));

        if (!existeProveedor) {
            log.warn("⚠️ Proveedor con ID {} no encontrado", idProveedor);
            return new CodigoResponse<>(404, "Proveedor no encontrado", List.of());
        }
        log.info("✅ Proveedor validado correctamente");

        List<HerramientasResponse> lista = herramientasRepository.findByIdProveedor(idProveedor)
                .stream()
                .map(this::mapHerramienta)
                .collect(Collectors.toList());

        log.info("✅ Se encontraron {} herramientas del proveedor {}", lista.size(), idProveedor);
        return new CodigoResponse<>(200, "Herramientas del proveedor obtenidas", lista);
    }
}