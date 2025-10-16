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

        // Manejar la fotografía como Base64 para PostgreSQL
        if (requestDTO.getFotografia() != null && !requestDTO.getFotografia().isEmpty()) {
            // Si viene como data URL completa (data:image/png;base64,...)
            if (requestDTO.getFotografia().startsWith("data:image")) {
                // Extraer solo la parte Base64 (después de la coma)
                String base64Data = requestDTO.getFotografia().split(",")[1];
                proveedor.setFotografia(base64Data);
            } else {
                // Si ya viene como Base64 puro
                proveedor.setFotografia(requestDTO.getFotografia());
            }
        } else {
            proveedor.setFotografia(null);
        }
    }

    @Override
    public ProveedorResponseDTO toResponseDTO(Proveedor proveedor) {
        ProveedorResponseDTO responseDTO = new ProveedorResponseDTO();
        responseDTO.setId(proveedor.getId());
        responseDTO.setNombreEmpresa(proveedor.getNombreEmpresa());
        responseDTO.setNumTelefono(proveedor.getNumTelefono());
        responseDTO.setMaterialProvee(proveedor.getMaterialProvee());

        // Convertir Base64 almacenado a data URL para la respuesta
        if (proveedor.getFotografia() != null && !proveedor.getFotografia().isEmpty()) {
            // Puedes detectar el tipo de imagen si lo guardaste por separado
            // Por simplicidad, usamos image/jpeg o image/png
            String mimeType = detectMimeType(proveedor.getFotografia());
            String dataUrl = "data:" + mimeType + ";base64," + proveedor.getFotografia();
            responseDTO.setFotografia(dataUrl);
        } else {
            responseDTO.setFotografia(null);
        }

        return responseDTO;
    }

    // Método auxiliar para detectar el tipo MIME (opcional)
    private String detectMimeType(String base64) {
        // Los primeros caracteres pueden indicar el tipo
        if (base64.startsWith("/9j") || base64.startsWith("/9J")) {
            return "image/jpeg";
        } else if (base64.startsWith("iVBOR")) {
            return "image/png";
        } else if (base64.startsWith("R0lGOD")) {
            return "image/gif";
        } else {
            return "image/jpeg"; // Por defecto
        }
    }
}

