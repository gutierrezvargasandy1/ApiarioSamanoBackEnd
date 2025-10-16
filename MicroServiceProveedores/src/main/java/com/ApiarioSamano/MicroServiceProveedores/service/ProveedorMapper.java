package com.ApiarioSamano.MicroServiceProveedores.service;

import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorRequestDTO;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorResponseDTO;
import com.ApiarioSamano.MicroServiceProveedores.model.Proveedor;
import org.springframework.stereotype.Component;

@Component
public class ProveedorMapper {

    public ProveedorResponseDTO toResponseDTO(Proveedor proveedor) {
        if (proveedor == null) return null;
        ProveedorResponseDTO dto = new ProveedorResponseDTO();
    dto.setId(proveedor.getId());
    dto.setNombreEmpresa(proveedor.getNombreEmpresa());
    dto.setNumTelefono(proveedor.getNumTelefono());
    dto.setMaterialProvee(proveedor.getMaterialProvee());
    dto.setFotografia(proveedor.getFotografia());
        return dto;
    }

    public void updateEntityFromDTO(ProveedorRequestDTO dto, Proveedor entity) {
        if (dto == null || entity == null) return;
        if (dto.getNombreEmpresa() != null) entity.setNombreEmpresa(dto.getNombreEmpresa());
        if (dto.getNumTelefono() != null) entity.setNumTelefono(dto.getNumTelefono());
        if (dto.getNombreReprecentante() != null) entity.setNombreReprecentante(dto.getNombreReprecentante());
        if (dto.getMaterialProvee() != null) entity.setMaterialProvee(dto.getMaterialProvee());
        // Nota: no tocamos fotografia aquí, se maneja por separado
    }
}
