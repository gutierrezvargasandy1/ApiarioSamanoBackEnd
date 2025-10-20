package com.ApiarioSamano.MicroServiceApiarios.dto.RecetaMedicamentoDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecetaMedicamentoRequest {
    private Long idReceta;
    private Long idMedicamento;
}