package com.ApiarioSamano.MicroServiceApiarios.dto.HistorialMedicoDTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistorialMedicoResponse {
    private Long id;
    private LocalDateTime fechaCreacion;
    private String descripcion;
}
