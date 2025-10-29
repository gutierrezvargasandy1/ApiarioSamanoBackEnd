package com.ApiarioSamano.MicroServiceAlmacen.dto.HerramientasDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HerramientasRequest {
    private String nombre;
    private String foto;
    private Integer idAlmacen;
    private Integer idProveedor;
}
