package com.ApiarioSamano.MicroServiceGeneradorCodigo.controller;

import org.springframework.web.bind.annotation.*;
import com.ApiarioSamano.MicroServiceGeneradorCodigo.dto.CodigoResponseDTO;
import com.ApiarioSamano.MicroServiceGeneradorCodigo.services.CodigoService;

@RestController
@RequestMapping("/api/codigos")
public class CodigoController implements ICodigoController {

    private final CodigoService codigoService;

    public CodigoController(CodigoService codigoService) {
        this.codigoService = codigoService;
    }

    @Override
    public CodigoResponseDTO generarOTP() {
        String codigo = codigoService.generarOTP();
        return new CodigoResponseDTO(
                codigo,
                "OK",
                "Código OTP generado correctamente."
        );
    }

    @Override
    public CodigoResponseDTO generarLote(String apiario, int numeroLote) {
        String codigo = codigoService.generarCodigoLote(apiario, numeroLote);
        return new CodigoResponseDTO(
                codigo,
                "OK",
                "Código de lote generado para el apiario " + apiario + "."
        );
    }

    @Override
    public CodigoResponseDTO generarAlmacen(String zona, String producto) {
        String codigo = codigoService.generarCodigoAlmacen(zona, producto);
        return new CodigoResponseDTO(
                codigo,
                "OK",
                "Código de almacén generado para el producto " + producto + "."
        );
    }
}
