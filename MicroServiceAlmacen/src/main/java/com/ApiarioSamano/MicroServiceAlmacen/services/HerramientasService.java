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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j // ✅ Añadido
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
        response.setFoto(h.getFoto());
        response.setIdProveedor(h.getIdProveedor());
        return response;
    }

    // ================== CRUD ==================
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

        Herramientas herramienta = new Herramientas();
        herramienta.setNombre(req.getNombre());
        herramienta.setFoto(req.getFoto());
        herramienta.setAlmacen(almacen);
        herramienta.setIdProveedor(req.getIdProveedor());

        log.info("💾 Guardando herramienta en base de datos...");
        Herramientas guardada = herramientasRepository.save(herramienta);
        log.info("✅ Herramienta guardada exitosamente con ID: {}", guardada.getId());

        return new CodigoResponse<>(200, "Herramienta guardada correctamente", mapHerramienta(guardada));
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

    public CodigoResponse<Void> eliminar(Long id) {
        log.info("🗑️ Intentando eliminar herramienta con ID: {}", id);
        if (herramientasRepository.existsById(id)) {
            herramientasRepository.deleteById(id);
            log.info("✅ Herramienta con ID {} eliminada correctamente", id);
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
            dto.setFoto(h.getFoto());

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
        dto.setFoto(herramienta.getFoto());

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