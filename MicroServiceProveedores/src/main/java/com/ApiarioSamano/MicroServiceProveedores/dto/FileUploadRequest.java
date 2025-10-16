package com.ApiarioSamano.MicroServiceProveedores.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadRequest {
    private String servicioOrigen;
    private String entidadOrigen;
    private String entidadId;
    private String campoAsociado;
}



