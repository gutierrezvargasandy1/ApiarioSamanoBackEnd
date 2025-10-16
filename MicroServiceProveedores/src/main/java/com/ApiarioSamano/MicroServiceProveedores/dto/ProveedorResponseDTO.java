package com.ApiarioSamano.MicroServiceProveedores.dto;
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
    private String fotografia; 

  
}