package com.ApiarioSamano.MicroServiceProveedores.service;

import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorRequestDTO;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorResponseDTO;
import com.ApiarioSamano.MicroServiceProveedores.model.Proveedor;

import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class ProveedorMapper implements IProveedorMapper {

    @Override
    public Proveedor toEntity(ProveedorRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        Proveedor proveedor = new Proveedor();
        proveedor.setNombreEmpresa(dto.getNombreEmpresa());
        proveedor.setNumTelefono(dto.getNumTelefono());
        proveedor.setMaterialProvee(dto.getMaterialProvee());
        proveedor.setFotografia(dto.getFotografia());
        return proveedor;
    }

    @Override
    public ProveedorResponseDTO toResponseDTO(Proveedor proveedor) {
        if (proveedor == null)
            return null;

        String base64Imagen = null;
        if (proveedor.getFotografia() != null) {
            base64Imagen = Base64.getEncoder().encodeToString(proveedor.getFotografia());
        }

        return ProveedorResponseDTO.builder()
                .id(proveedor.getId())
                .nombreEmpresa(proveedor.getNombreEmpresa())
                .numTelefono(proveedor.getNumTelefono())
                .materialProvee(proveedor.getMaterialProvee())
                .fotografia(base64Imagen)
                .build();
    }

    @Override
    public void updateEntityFromDTO(ProveedorRequestDTO dto, Proveedor proveedor) {
        if (dto != null && proveedor != null) {
            proveedor.setNombreEmpresa(dto.getNombreEmpresa());
            proveedor.setNumTelefono(dto.getNumTelefono());
            proveedor.setMaterialProvee(dto.getMaterialProvee());
            proveedor.setFotografia(dto.getFotografia());
        }
    }
}