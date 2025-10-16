package com.ApiarioSamano.MicroServiceProveedores.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProveedorRequestDTO {

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(min = 2, max = 200, message = "El nombre debe tener entre 2 y 200 caracteres")
    private String nombreEmpresa;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String numTelefono;

    @NotBlank(message = "El nombre de reprecentante de la empresa es obligatorio")
    @Size(min = 2, max = 200, message = "El nombre debe tener entre 2 y 200 caracteres")
    private String nombreReprecentante;

    @Size(max = 200, message = "El material no puede exceder 200 caracteres")
    private String materialProvee;

    private String fotografia;
}