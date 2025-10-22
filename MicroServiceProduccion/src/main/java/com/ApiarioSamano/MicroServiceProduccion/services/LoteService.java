package com.ApiarioSamano.MicroServiceProduccion.services;

import com.ApiarioSamano.MicroServiceProduccion.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceProduccion.dto.LoteDTO.LoteConAlmacenResponse;
import com.ApiarioSamano.MicroServiceProduccion.dto.LoteDTO.LoteRequest;
import com.ApiarioSamano.MicroServiceProduccion.dto.MicroServiceClientAlmaceneDTO.AlmacenResponse;
import com.ApiarioSamano.MicroServiceProduccion.dto.MicroServiceClientGeneradorCoigoDTO.LoteRequestClient;
import com.ApiarioSamano.MicroServiceProduccion.model.Lote;
import com.ApiarioSamano.MicroServiceProduccion.repository.LoteRepository;
import com.ApiarioSamano.MicroServiceProduccion.services.MicroServiceClientAlmacen.AlmacenClientMicroservice;
import com.ApiarioSamano.MicroServiceProduccion.services.MicroServiceClientGeneradorCodigo.CodigoClientMicroservice;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoteService {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private final AlmacenClientMicroservice almacenClientMicroservice;
    
    @Autowired
    private final CodigoClientMicroservice codigoClientMicroservice;

    private static final Logger log = LoggerFactory.getLogger(LoteService.class);


  public CodigoResponse<Lote> guardarLote(LoteRequest request) {
    try {
        log.info("Validando existencia del almacén con ID: {}", request.getIdAlmacen());

        // 🔹 Verificar que el almacén exista llamando al microservicio
        var responseAlmacen = almacenClientMicroservice.obtenerAlmacenPorId(request.getIdAlmacen());
        if (responseAlmacen == null || responseAlmacen.getCodigo() != 200 || responseAlmacen.getData() == null) {
            log.warn("El almacén con ID {} no existe o no fue encontrado", request.getIdAlmacen());
            return new CodigoResponse<>(404, "El almacén especificado no existe", null);
        }
        
        // 🔹 Crear nuevo lote
        Lote lote = new Lote();
        lote.setNumeroSeguimiento(null);
        lote.setTipoProducto(request.getTipoProducto());
        lote.setFechaCreacion(LocalDate.now());
        lote.setIdAlmacen(request.getIdAlmacen());

        // 🔹 Guardar en base de datos
        Lote guardado = loteRepository.save(lote);
        log.info("Lote guardado correctamente con ID: {}", guardado.getId());

        LoteRequestClient infLote = new LoteRequestClient() ;
        infLote.setProducto(request.getTipoProducto());
        infLote.setNumeroLote(guardado.getId().intValue());

        String codigoSerguimiento = codigoClientMicroservice.generarLote(infLote);

         // 🔹 Crear nuevo lote
        Lote loteConCodigo = new Lote();
        loteConCodigo.setNumeroSeguimiento(codigoSerguimiento);
        loteConCodigo.setTipoProducto(guardado.getTipoProducto());
        loteConCodigo.setFechaCreacion(LocalDate.now());
        loteConCodigo.setIdAlmacen(guardado.getIdAlmacen());

           // 🔹 Guardar en base de datos
        Lote guardadoConCodigo = loteRepository.save(loteConCodigo);
        log.info("Lote guardado correctamente con ID: {}", guardadoConCodigo.getId());
        return new CodigoResponse<>(200, "Lote guardado correctamente", guardado);

    } catch (Exception e) {
        log.error("Error al guardar el lote: {}", e.getMessage(), e);
        return new CodigoResponse<>(500, "Error al guardar el lote: " + e.getMessage(), null);
    }
}


    public CodigoResponse<List<Lote>> listarLotes() {
        List<Lote> lista = loteRepository.findAll();
        return new CodigoResponse<>(200, "Listado de lotes obtenido correctamente", lista);
    }

     /**
     * 🔹 Obtiene un lote por su ID junto con la información del almacén desde otro microservicio.
     */
    public CodigoResponse<LoteConAlmacenResponse> obtenerLoteConAlmacenPorId(Long idLote) {
        try {
            log.info("Buscando lote con ID: {}", idLote);

            Optional<Lote> optionalLote = loteRepository.findById(idLote);

            if (optionalLote.isEmpty()) {
                log.warn("No se encontró el lote con ID {}", idLote);
                return new CodigoResponse<>(404, "Lote no encontrado", null);
            }

            Lote lote = optionalLote.get();
            AlmacenResponse almacenResponse = null;

            try {
                var almacenResponseData = almacenClientMicroservice.obtenerAlmacenPorId(lote.getIdAlmacen());
                if (almacenResponseData.getCodigo() == 200) {
                    almacenResponse = almacenResponseData.getData();
                } else {
                    log.warn("El microservicio de almacén no devolvió datos válidos para el ID {}", lote.getIdAlmacen());
                }
            } catch (Exception e) {
                log.error("Error al obtener información del almacén para el lote {}: {}", lote.getId(), e.getMessage());
            }

            LoteConAlmacenResponse respuesta = new LoteConAlmacenResponse(
                    lote.getId(),
                    lote.getNumeroSeguimiento(),
                    lote.getTipoProducto(),
                    lote.getFechaCreacion(),
                    lote.getIdAlmacen(),
                    almacenResponse
            );

            return new CodigoResponse<>(200, "Lote con almacén obtenido correctamente", respuesta);

        } catch (Exception e) {
            log.error("Error al obtener lote con almacén: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al obtener el lote con su almacén", null);
        }
    }


    public CodigoResponse<Void> eliminarLote(Long id) {
        if (!loteRepository.existsById(id)) {
            return new CodigoResponse<>(404, "Lote no encontrado", null);
        }

        loteRepository.deleteById(id);
        return new CodigoResponse<>(200, "Lote eliminado correctamente", null);
    }
}
