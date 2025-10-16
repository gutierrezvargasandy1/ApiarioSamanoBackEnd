package com.ApiarioSamano.MicroServiceProveedores.service;

import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorRequestDTO;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorResponseDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProveedorService {

    private final List<ProveedorResponseDTO> proveedores = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong();

    // Obtener todos los proveedores
    public List<ProveedorResponseDTO> getAllProveedores() {
        return new ArrayList<>(proveedores);
    }

    // Obtener proveedor por ID
    public Optional<ProveedorResponseDTO> getProveedorById(Long id) {
        return proveedores.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    // Crear proveedor
    public ProveedorResponseDTO createProveedor(ProveedorRequestDTO requestDTO) {
        ProveedorResponseDTO proveedor = ProveedorResponseDTO.builder()
                .id(idGenerator.incrementAndGet())
                .nombreEmpresa(requestDTO.getNombreEmpresa())
                .numTelefono(requestDTO.getNumTelefono())
                .materialProvee(requestDTO.getMaterialProvee())
                .fotografia(requestDTO.getFotografia())
                .build();

        proveedores.add(proveedor);
        return proveedor;
    }

    // Actualizar proveedor
    public ProveedorResponseDTO updateProveedor(Long id, ProveedorRequestDTO requestDTO) {
        Optional<ProveedorResponseDTO> optionalProveedor = getProveedorById(id);

        if (optionalProveedor.isEmpty()) {
            throw new RuntimeException("Proveedor no encontrado con ID: " + id);
        }

        ProveedorResponseDTO proveedor = optionalProveedor.get();
        proveedor.setNombreEmpresa(requestDTO.getNombreEmpresa());
        proveedor.setNumTelefono(requestDTO.getNumTelefono());
        proveedor.setMaterialProvee(requestDTO.getMaterialProvee());
        proveedor.setFotografia(requestDTO.getFotografia());

        return proveedor;
    }

    // Eliminar proveedor
    public void deleteProveedor(Long id) {
        proveedores.removeIf(p -> p.getId().equals(id));
    }
}
