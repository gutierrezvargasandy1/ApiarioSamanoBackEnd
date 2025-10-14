package com.ApiarioSamano.MicroServiceProveedores.service;

import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorRequestDTO;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorResponseDTO;
import java.util.List;

/**
 * Interfaz que define el contrato para las operaciones de Proveedor
 * Proporciona desacoplamiento entre la capa de controlador y servicio
 */
public interface IProveedorService {

    /**
     * Crea un nuevo proveedor a partir del DTO de solicitud
     * 
     * @param requestDTO Datos del proveedor a crear (sin ID)
     * @return ProveedorResponseDTO con los datos creados incluyendo el ID generado
     */
    ProveedorResponseDTO crearProveedor(ProveedorRequestDTO requestDTO);

    /**
     * Obtiene un proveedor específico por su ID
     * 
     * @param id Identificador del proveedor
     * @return ProveedorResponseDTO con los datos del proveedor
     * @throws RuntimeException si el proveedor no existe
     */
    ProveedorResponseDTO obtenerProveedorPorId(Long id);

    /**
     * Obtiene la lista de todos los proveedores
     * 
     * @return Lista de ProveedorResponseDTO
     */
    List<ProveedorResponseDTO> obtenerTodosProveedores();

    /**
     * Actualiza un proveedor existente
     * 
     * @param id         Identificador del proveedor a actualizar
     * @param requestDTO Datos actualizados del proveedor (sin ID)
     * @return ProveedorResponseDTO con los datos actualizados
     * @throws RuntimeException si el proveedor no existe
     */
    ProveedorResponseDTO actualizarProveedor(Long id, ProveedorRequestDTO requestDTO);

    /**
     * Elimina un proveedor por su ID
     * 
     * @param id Identificador del proveedor a eliminar
     * @throws RuntimeException si el proveedor no existe
     */
    void eliminarProveedor(Long id);
}