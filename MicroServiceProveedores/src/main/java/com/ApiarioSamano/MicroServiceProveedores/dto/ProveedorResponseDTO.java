package com.ApiarioSamano.MicroServiceProveedores.dto;

import java.util.Base64;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProveedorResponseDTO {
    private Long id;
    private String nombreEmpresa;
    private String numTelefono;
    private String materialProvee;
    private String fotografia; // Solo Base64

    public void setFotografia(byte[] fotografiaBytes) {
        if (fotografiaBytes != null && fotografiaBytes.length > 0) {
            this.fotografia = Base64.getEncoder().encodeToString(fotografiaBytes);
        } else {
            this.fotografia = null;
        }
    }
}