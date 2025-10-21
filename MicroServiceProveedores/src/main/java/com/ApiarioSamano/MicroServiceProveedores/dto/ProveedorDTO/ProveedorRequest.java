package com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProveedorRequest {
    private byte[] fotografia;
    private String nombreEmpresa;
    private String nombreReprecentante;
    private String numTelefono;
    private String materialProvee;
}
