package com.ApiarioSamano.MicroServiceProveedores.service;

import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorRequestDTO;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorResponseDTO;
import com.ApiarioSamano.MicroServiceProveedores.model.Proveedor;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class ProveedorMapper implements IProveedorMapper {

    @Override
    public Proveedor toEntity(ProveedorRequestDTO requestDTO) {
        Proveedor proveedor = new Proveedor();
        updateEntityFromDTO(requestDTO, proveedor);
        return proveedor;
    }

    @Override
    public void updateEntityFromDTO(ProveedorRequestDTO requestDTO, Proveedor proveedor) {
        proveedor.setNombreEmpresa(requestDTO.getNombreEmpresa());
        proveedor.setNumTelefono(requestDTO.getNumTelefono());
        proveedor.setMaterialProvee(requestDTO.getMaterialProvee());

        // Convertir MultipartFile a byte[]
        if (requestDTO.getFotografia() != null && !requestDTO.getFotografia().isEmpty()) {
            try {
                proveedor.setFotografia(requestDTO.getFotografia().getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Error al procesar la imagen: " + e.getMessage());
            }
        } else {
            // Si no se envía nueva imagen, mantener la existente (en caso de actualización)
            // Para creación, se queda como null
        }
    }

    @Override
    public ProveedorResponseDTO toResponseDTO(Proveedor proveedor) {
        ProveedorResponseDTO responseDTO = new ProveedorResponseDTO();
        responseDTO.setId(proveedor.getId());
        responseDTO.setNombreEmpresa(proveedor.getNombreEmpresa());
        responseDTO.setNumTelefono(proveedor.getNumTelefono());
        responseDTO.setMaterialProvee(proveedor.getMaterialProvee());

        // USAR EL MÉTODO setFotografia() QUE CONVIERTE A Base64 AUTOMÁTICAMENTE
        responseDTO.setFotografia(proveedor.getFotografia());

        return responseDTO;
    }
}