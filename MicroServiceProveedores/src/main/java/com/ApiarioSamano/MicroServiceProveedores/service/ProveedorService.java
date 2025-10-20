package com.ApiarioSamano.MicroServiceProveedores.service;

import com.ApiarioSamano.MicroServiceProveedores.dto.FileUploadResponse;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorRequestDTO;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorResponseDTO;

import io.jsonwebtoken.io.IOException;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService {

    private final List<ProveedorResponseDTO> proveedores = new ArrayList<>();
    private final MicroServiceGestorArchivosClient archivosClient;

    public ProveedorService(MicroServiceGestorArchivosClient archivosClient) {
        this.archivosClient = archivosClient;
    }

    // Obtener todos los proveedores
    public List<ProveedorResponseDTO> obtenerTodosProveedores() {
        List<ProveedorResponseDTO> result = new ArrayList<>();
        for (ProveedorResponseDTO p : proveedores) {
            ProveedorResponseDTO dto = new ProveedorResponseDTO(p);
            if (p.getFotografia() != null) {
                // Reemplazamos el fileId por la URL de vista
                dto.setFotografiaUrl("/api/ver/view/" + p.getFotografia());
            }
            result.add(dto);
        }
        return result;
    }

    // Obtener proveedor por ID
    public ProveedorResponseDTO obtenerProveedorPorId(Long id) {
        Optional<ProveedorResponseDTO> optionalProveedor = proveedores.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();

        if (optionalProveedor.isEmpty()) {
            throw new RuntimeException("Proveedor no encontrado con ID: " + id);
        }

        ProveedorResponseDTO proveedor = new ProveedorResponseDTO(optionalProveedor.get());
        if (proveedor.getFotografia() != null) {
            proveedor.setFotografiaUrl("/api/ver/view/" + proveedor.getFotografia());
        }

        return proveedor;
    }

    // Crear proveedor
    public ProveedorResponseDTO crearProveedor(ProveedorRequestDTO requestDTO) throws IOException, java.io.IOException {
        String idFoto = null;

        if (requestDTO.getFotografia() != null && !requestDTO.getFotografia().isEmpty()) {
            FileUploadResponse response = archivosClient.uploadFile(requestDTO.getFotografia());
            idFoto = response.getFileId();
        }

        ProveedorResponseDTO proveedor = ProveedorResponseDTO.builder()
                .nombreEmpresa(requestDTO.getNombreEmpresa())
                .numTelefono(requestDTO.getNumTelefono())
                .materialProvee(requestDTO.getMaterialProvee())
                .fotografia(idFoto)
                .build();

        proveedores.add(proveedor);

        // Agregar URL directa
        if (idFoto != null) {
            proveedor.setFotografiaUrl("/api/ver/view/" + idFoto);
        }

        return proveedor;
    }

    // Actualizar proveedor
    public ProveedorResponseDTO actualizarProveedor(Long id, ProveedorRequestDTO requestDTO)
            throws java.io.IOException {
        Optional<ProveedorResponseDTO> optionalProveedor = proveedores.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();

        if (optionalProveedor.isEmpty()) {
            throw new RuntimeException("Proveedor no encontrado con ID: " + id);
        }

        ProveedorResponseDTO proveedor = optionalProveedor.get();
        proveedor.setNombreEmpresa(requestDTO.getNombreEmpresa());
        proveedor.setNumTelefono(requestDTO.getNumTelefono());
        proveedor.setMaterialProvee(requestDTO.getMaterialProvee());

        // Actualizar fotografía si hay nueva
        if (requestDTO.getFotografia() != null && !requestDTO.getFotografia().isEmpty()) {
            try {
                FileUploadResponse response = archivosClient.uploadFile(requestDTO.getFotografia());
                proveedor.setFotografia(response.getFileId());
                proveedor.setFotografiaUrl("/api/ver/view/" + response.getFileId());
            } catch (IOException e) {
                throw new RuntimeException("Error al subir la nueva foto: " + e.getMessage());
            }
        }

        return proveedor;
    }

    // Eliminar proveedor
    public void eliminarProveedor(Long id) {
        proveedores.removeIf(p -> p.getId().equals(id));
    }
}
