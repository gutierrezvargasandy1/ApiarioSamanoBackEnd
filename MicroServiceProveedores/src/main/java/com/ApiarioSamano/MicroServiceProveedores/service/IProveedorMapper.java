package com.ApiarioSamano.MicroServiceProveedores.service;

import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorRequestDTO;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorResponseDTO;
import com.ApiarioSamano.MicroServiceProveedores.model.Proveedor;

public interface IProveedorMapper {
    Proveedor toEntity(ProveedorRequestDTO dto);

    ProveedorResponseDTO toResponseDTO(Proveedor proveedor);

    void updateEntityFromDTO(ProveedorRequestDTO dto, Proveedor proveedor);
}