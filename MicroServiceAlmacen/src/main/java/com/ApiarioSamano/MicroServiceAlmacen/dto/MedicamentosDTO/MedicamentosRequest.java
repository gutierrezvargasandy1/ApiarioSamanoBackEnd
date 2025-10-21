package com.ApiarioSamano.MicroServiceAlmacen.dto.MedicamentosDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicamentosRequest {
    private String nombre;
    private String descripcion;
    private Integer idAlmacen;
    private String cantidad;
    private Integer idProveedor;
    private byte[] foto;
}
