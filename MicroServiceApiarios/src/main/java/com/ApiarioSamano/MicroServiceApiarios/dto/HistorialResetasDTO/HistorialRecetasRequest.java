package com.ApiarioSamano.MicroServiceApiarios.dto.HistorialResetasDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialRecetasRequest {
    private Integer idHistorialMedico;
    private Integer idReceta;
}
