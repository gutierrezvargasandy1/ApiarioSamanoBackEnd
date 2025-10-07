package com.ApiarioSamano.MicroServiceGeneradorCodigo.controller;

import com.ApiarioSamano.MicroServiceGeneradorCodigo.dto.CodigoResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface ICodigoController {

    @GetMapping("/otp")
    CodigoResponseDTO generarOTP();

    @GetMapping("/lote")
    CodigoResponseDTO generarLote(
            @RequestParam(defaultValue = "A") String apiario,
            @RequestParam(defaultValue = "1") int numeroLote
    );

    @GetMapping("/almacen")
    CodigoResponseDTO generarAlmacen(
            @RequestParam(defaultValue = "Z1") String zona,
            @RequestParam(defaultValue = "PRD") String producto
    );
}
