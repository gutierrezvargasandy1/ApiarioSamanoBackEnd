package com.ApiarioSamano.MicroServiceProveedores.controller;

import com.ApiarioSamano.MicroServiceProveedores.dto.ApiResponse;
import com.ApiarioSamano.MicroServiceProveedores.dto.CodigoEstatus;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorRequestDTO;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorResponseDTO;
import com.ApiarioSamano.MicroServiceProveedores.service.ProveedorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProveedorResponseDTO>>> listarProveedores() {
        try {
            List<ProveedorResponseDTO> proveedores = proveedorService.obtenerTodosProveedores();
            return ResponseEntity.ok(ApiResponse.success(proveedores, "Proveedores obtenidos exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(CodigoEstatus.ERROR_INTERNO, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProveedorResponseDTO>> obtenerProveedorPorId(@PathVariable Long id) {
        try {
            Optional<ProveedorResponseDTO> proveedor = Optional.ofNullable(proveedorService.obtenerProveedorPorId(id));
            if (proveedor.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(proveedor.get(), "Proveedor obtenido exitosamente"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.notFound("Proveedor no encontrado con ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(CodigoEstatus.ERROR_INTERNO, e.getMessage()));
        }
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProveedorResponseDTO>> crearProveedor(
            @Valid @ModelAttribute ProveedorRequestDTO requestDTO) {
        try {
            ProveedorResponseDTO nuevoProveedor = proveedorService.crearProveedor(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(nuevoProveedor, "Proveedor creado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(CodigoEstatus.SOLICITUD_INCORRECTA, e.getMessage()));
        }
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProveedorResponseDTO>> actualizarProveedor(
            @PathVariable Long id,
            @Valid @ModelAttribute ProveedorRequestDTO requestDTO) throws IOException {
        try {
            ProveedorResponseDTO actualizado = proveedorService.actualizarProveedor(id, requestDTO);
            return ResponseEntity.ok(ApiResponse.success(actualizado, "Proveedor actualizado exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarProveedor(@PathVariable Long id) {
        try {
            proveedorService.eliminarProveedor(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Proveedor eliminado exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound(e.getMessage()));
        }
    }
}
