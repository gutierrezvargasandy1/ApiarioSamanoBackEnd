package com.ApiarioSamano.MicroServiceApiarios.dto.RecetaMedicamentoDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecetaMedicamentoResponse {
    private Long id;
    private Long idReceta;
    private Long idMedicamento;
}