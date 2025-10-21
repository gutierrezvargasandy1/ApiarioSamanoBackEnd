package com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MateriasPrimasRequest {
    private String nombre;
    private byte[] foto;
    private BigDecimal cantidad;
    private Long idAlmacen;
    private Integer idProvedor;
}
