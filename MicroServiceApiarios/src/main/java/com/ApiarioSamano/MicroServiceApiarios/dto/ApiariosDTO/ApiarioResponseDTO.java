package com.ApiarioSamano.MicroServiceApiarios.dto.ApiariosDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiarioResponseDTO {
    private Long id;
    private Long numeroApiairo;
    private String ubicacion;
    private String salud;
    private Long idHistorialMedico;
    private Long idReceta;

}