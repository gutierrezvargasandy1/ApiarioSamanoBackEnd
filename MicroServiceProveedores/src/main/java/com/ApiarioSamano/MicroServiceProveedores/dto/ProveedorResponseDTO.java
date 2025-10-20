package com.ApiarioSamano.MicroServiceProveedores.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ProveedorResponseDTO {
    public ProveedorResponseDTO(ProveedorResponseDTO otro) {
        this.id = otro.getId();
        this.nombreEmpresa = otro.getNombreEmpresa();
        this.numTelefono = otro.getNumTelefono();
        this.nombreRepresentante = otro.getNombreRepresentante();
        this.materialProvee = otro.getMaterialProvee();
        this.fotografia = otro.getFotografia();
        // Generamos la URL automáticamente
        this.fotografiaUrl = "/api/files/view/" + otro.getFotografia();
    }

    private Long id;
    private String nombreEmpresa;
    private String numTelefono;
    private String materialProvee;
    private String fotografia;
    private String fotografiaUrl;
    private String nombreRepresentante;

}