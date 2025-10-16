package com.ApiarioSamano.MicroServiceProveedores.service;

import com.ApiarioSamano.MicroServiceProveedores.dto.ArchivoDTO;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorRequestDTO;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorResponseDTO;
import com.ApiarioSamano.MicroServiceProveedores.model.Proveedor;
import com.ApiarioSamano.MicroServiceProveedores.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProveedorService implements IProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final IProveedorMapper proveedorMapper;
    private final MicroServiceClient microServiceClient; // 👈 Agregamos el cliente del microservicio de archivos

    @Override
    public ProveedorResponseDTO crearProveedor(ProveedorRequestDTO requestDTO) {
        // 1️⃣ Guardamos primero el proveedor (sin foto)
        Proveedor proveedor = proveedorMapper.toEntity(requestDTO);
        Proveedor guardado = proveedorRepository.save(proveedor);

        // 2️⃣ Si viene una foto en el DTO, la subimos al microservicio de archivos
        if (requestDTO.getFotografia() != null && !requestDTO.getFotografia().isEmpty()) {
            File file = new File(requestDTO.getFotografia());
            if (file.exists()) {
                ArchivoDTO archivoDTO = microServiceClient.uploadFile(
                        file,
                        "MicroServiceProveedores", // servicio origen
                        "Proveedor",               // entidad origen
                        guardado.getId().toString(),
                        "fotografia"               // campo asociado
                );
                // 3️⃣ Guardamos el fileId devuelto en la base de datos
                guardado.setFotografia(archivoDTO.getId());
                proveedorRepository.save(guardado);
            }
        }

        return proveedorMapper.toResponseDTO(guardado);
    }

    @Override
    public ProveedorResponseDTO obtenerProveedorPorId(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));

        ProveedorResponseDTO response = proveedorMapper.toResponseDTO(proveedor);

        // 4️⃣ Si tiene una foto, la obtenemos desde el microservicio de archivos
        if (proveedor.getFotografia() != null) {
            ArchivoDTO archivoDTO = microServiceClient.getFileByField(
                    "MicroServiceProveedores",
                    "Proveedor",
                    proveedor.getId().toString(),
                    "fotografia"
            );
            if (archivoDTO != null) {
                response.setFotografia(archivoDTO.getRutaCompleta()); // Asumiendo que tu DTO tiene campo url
            }
        }

        return response;
    }

    @Override
    public List<ProveedorResponseDTO> obtenerTodosProveedores() {
        return proveedorRepository.findAll()
                .stream()
                .map(proveedor -> {
                    ProveedorResponseDTO dto = proveedorMapper.toResponseDTO(proveedor);
                    if (proveedor.getFotografia() != null) {
                        ArchivoDTO archivoDTO = microServiceClient.getFileByField(
                                "MicroServiceProveedores",
                                "Proveedor",
                                proveedor.getId().toString(),
                                "fotografia"
                        );
                        if (archivoDTO != null) dto.setFotografia(archivoDTO.getRutaCompleta());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ProveedorResponseDTO actualizarProveedor(Long id, ProveedorRequestDTO requestDTO) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));

        proveedorMapper.updateEntityFromDTO(requestDTO, proveedor);
        Proveedor actualizado = proveedorRepository.save(proveedor);

        // 5️⃣ Si se actualiza la foto, volvemos a subirla
        if (requestDTO.getFotografia() != null && !requestDTO.getFotografia().isEmpty()) {
            File file = new File(requestDTO.getFotografia());
            if (file.exists()) {
                ArchivoDTO archivoDTO = microServiceClient.uploadFile(
                        file,
                        "MicroServiceProveedores",
                        "Proveedor",
                        actualizado.getId().toString(),
                        "fotografia"
                );
                actualizado.setFotografia(archivoDTO.getId());
                proveedorRepository.save(actualizado);
            }
        }

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
