package com.ApiarioSamano.MicroServiceAlmacen.services;

import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.AlmacenDTO.AlmacenRequest;
import com.ApiarioSamano.MicroServiceAlmacen.dto.GeneradorCodigoClient.AlmacenRequestClient;
import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import com.ApiarioSamano.MicroServiceAlmacen.repository.AlmacenRepository;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AlmacenService {

    @Autowired
    private AlmacenRepository almacenRepository;
    @Autowired
    private GeneradorCodigoClient generadorCodigoClient;

    // Crear o actualizar
    public CodigoResponse<Almacen> guardarAlmacen(AlmacenRequest almacenRequest) {
        // Llamar al microservicio para generar el código
        log.info("Solicitando código al microservicio de generador de códigos");
        log.info("Solicitando código al microservicio de generador de códigos");

        String codigoResponse = generadorCodigoClient.generarAlmacen(
                new AlmacenRequestClient(almacenRequest.getUbicacion(), "AlmacenGeneral"));
        log.info("Código generado recibido del microservicio: {}", codigoResponse);

        // Crear la entidad Almacen
        Almacen nuevoAlmacen = new Almacen();
        nuevoAlmacen.setNumeroSeguimiento(codigoResponse);
        nuevoAlmacen.setUbicacion(almacenRequest.getUbicacion());
        nuevoAlmacen.setEspaciosOcupados(almacenRequest.getEspaciosOcupados());
        nuevoAlmacen.setCapacidad(almacenRequest.getCapacidad());

        // Guardar en la base de datos
        Almacen almacenGuardado = almacenRepository.save(nuevoAlmacen);

        return new CodigoResponse<>(200, "Almacén guardado correctamente", almacenGuardado);
    }

    // Obtener todos
    public CodigoResponse<List<Almacen>> obtenerTodos() {
        List<Almacen> lista = almacenRepository.findAll();
        return new CodigoResponse<>(200, "Lista de almacenes obtenida", lista);
    }

    // Obtener por ID
    public CodigoResponse<Almacen> obtenerPorId(Long id) {
        Optional<Almacen> opt = almacenRepository.findById(id);
        if (opt.isPresent()) {
            return new CodigoResponse<>(200, "Almacén encontrado", opt.get());
        }
        return new CodigoResponse<>(404, "Almacén no encontrado", null);
    }

    // Eliminar por ID
    public CodigoResponse<Void> eliminarPorId(Long id) {
        if (almacenRepository.existsById(id)) {
            almacenRepository.deleteById(id);
            return new CodigoResponse<>(200, "Almacén eliminado correctamente", null);
        }
        return new CodigoResponse<>(404, "Almacén no encontrado", null);
    }
}
