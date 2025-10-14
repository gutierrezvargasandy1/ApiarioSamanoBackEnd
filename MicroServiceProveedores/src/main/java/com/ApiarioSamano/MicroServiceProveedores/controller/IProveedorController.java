package com.ApiarioSamano.MicroServiceProveedores.controller;

import com.ApiarioSamano.MicroServiceProveedores.dto.ApiResponse;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorRequestDTO;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface IProveedorController {
    ResponseEntity<ApiResponse<List<ProveedorResponseDTO>>> listarProveedores();

    ResponseEntity<ApiResponse<ProveedorResponseDTO>> obtenerProveedorPorId(@PathVariable Long id);

    ResponseEntity<ApiResponse<ProveedorResponseDTO>> crearProveedor(@RequestBody ProveedorRequestDTO requestDTO);

    ResponseEntity<ApiResponse<ProveedorResponseDTO>> actualizarProveedor(@PathVariable Long id,
            @RequestBody ProveedorRequestDTO requestDTO);

    ResponseEntity<ApiResponse<Void>> eliminarProveedor(@PathVariable Long id);
}