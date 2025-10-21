package com.ApiarioSamano.MicroServiceAlmacen.dto.AlmacenDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlmacenRequest {
    private Long id;
    private String numeroSeguimiento;
    private String ubicacion;
    private Integer espaciosOcupados;
    private Integer capacidad;

}
