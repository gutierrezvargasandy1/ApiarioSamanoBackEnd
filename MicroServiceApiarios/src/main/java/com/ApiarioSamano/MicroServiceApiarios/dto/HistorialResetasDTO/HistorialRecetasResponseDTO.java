package com.ApiarioSamano.MicroServiceApiarios.dto.HistorialResetasDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialRecetasResponseDTO {
    private Long idHistorialMedico;
    private List<Long> recetasIds;
}
