package com.ApiarioSamano.MicroServiceProveedores.controller;

import com.ApiarioSamano.MicroServiceProveedores.dto.ApiResponse;
import com.ApiarioSamano.MicroServiceProveedores.dto.CodigoEstatus;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorRequestDTO;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ApiarioSamano.MicroServiceProveedores.service.IProveedorService;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController implements IProveedorController {

    private final IProveedorService proveedorService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProveedorResponseDTO>>> listarProveedores() {
        try {
            List<ProveedorResponseDTO> proveedores = proveedorService.obtenerTodosProveedores();
            ApiResponse<List<ProveedorResponseDTO>> response = ApiResponse.success(proveedores,
                    "Proveedores obtenidos exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<ProveedorResponseDTO>> response = ApiResponse.error(CodigoEstatus.ERROR_INTERNO,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProveedorResponseDTO>> obtenerProveedorPorId(@PathVariable Long id) {
        try {
            ProveedorResponseDTO proveedor = proveedorService.obtenerProveedorPorId(id);
            ApiResponse<ProveedorResponseDTO> response = ApiResponse.success(proveedor,
                    "Proveedor obtenido exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<ProveedorResponseDTO> response = ApiResponse.notFound(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // CAMBIO IMPORTANTE: @ModelAttribute en lugar de @RequestBody
    @Override
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProveedorResponseDTO>> crearProveedor(
            @Valid @ModelAttribute ProveedorRequestDTO requestDTO) {
        try {
            ProveedorResponseDTO nuevoProveedor = proveedorService.crearProveedor(requestDTO);
            ApiResponse<ProveedorResponseDTO> response = ApiResponse.created(nuevoProveedor,
                    "Proveedor creado exitosamente");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<ProveedorResponseDTO> response = ApiResponse.error(CodigoEstatus.SOLICITUD_INCORRECTA,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // CAMBIO IMPORTANTE: @ModelAttribute en lugar de @RequestBody
    @Override
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProveedorResponseDTO>> actualizarProveedor(
            @PathVariable Long id,
            @Valid @ModelAttribute ProveedorRequestDTO requestDTO) {
        try {
            ProveedorResponseDTO actualizado = proveedorService.actualizarProveedor(id, requestDTO);
            ApiResponse<ProveedorResponseDTO> response = ApiResponse.success(actualizado,
                    "Proveedor actualizado exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<ProveedorResponseDTO> response = ApiResponse.notFound(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarProveedor(@PathVariable Long id) {
        try {
            proveedorService.eliminarProveedor(id);
            ApiResponse<Void> response = ApiResponse.success(null, "Proveedor eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<Void> response = ApiResponse.notFound(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}