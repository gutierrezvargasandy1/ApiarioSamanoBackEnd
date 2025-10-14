package com.ApiarioSamano.MicroServiceProveedores.service;

import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorRequestDTO;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorResponseDTO;
import com.ApiarioSamano.MicroServiceProveedores.model.Proveedor;
import com.ApiarioSamano.MicroServiceProveedores.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProveedorService implements IProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final IProveedorMapper proveedorMapper;

    @Override
    public ProveedorResponseDTO crearProveedor(ProveedorRequestDTO requestDTO) {
        Proveedor proveedor = proveedorMapper.toEntity(requestDTO);
        Proveedor guardado = proveedorRepository.save(proveedor);
        return proveedorMapper.toResponseDTO(guardado);
    }

    @Override
    public ProveedorResponseDTO obtenerProveedorPorId(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));
        return proveedorMapper.toResponseDTO(proveedor);
    }

    @Override
    public List<ProveedorResponseDTO> obtenerTodosProveedores() {
        return proveedorRepository.findAll()
                .stream()
                .map(proveedorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProveedorResponseDTO actualizarProveedor(Long id, ProveedorRequestDTO requestDTO) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));

        proveedorMapper.updateEntityFromDTO(requestDTO, proveedor);
        Proveedor actualizado = proveedorRepository.save(proveedor);
        return proveedorMapper.toResponseDTO(actualizado);
    }

    @Override
    public void eliminarProveedor(Long id) {
        if (!proveedorRepository.existsById(id)) {
            throw new RuntimeException("Proveedor no encontrado con ID: " + id);
        }
        proveedorRepository.deleteById(id);
    }
}