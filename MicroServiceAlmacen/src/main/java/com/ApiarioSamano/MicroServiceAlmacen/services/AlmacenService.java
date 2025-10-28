package com.ApiarioSamano.MicroServiceAlmacen.services;

import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.AlmacenDTO.*;
import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import com.ApiarioSamano.MicroServiceAlmacen.repository.AlmacenRepository;
import com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI.GeneradorCodigoClient;
import com.ApiarioSamano.MicroServiceAlmacen.dto.GeneradorCodigoClient.AlmacenRequestClient;
import com.ApiarioSamano.MicroServiceAlmacen.dto.HerramientasDTO.HerramientasResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO.MateriasPrimasResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MedicamentosDTO.MedicamentosResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AlmacenService {

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private GeneradorCodigoClient generadorCodigoClient;

    // Crear o actualizar
    public CodigoResponse<AlmacenResponse> guardarAlmacen(AlmacenRequest almacenRequest) {
        try {
            log.info("Solicitando código al microservicio de generador de códigos");

            String codigoResponse = generadorCodigoClient.generarAlmacen(
                    new AlmacenRequestClient(almacenRequest.getUbicacion(), "AlmacenGeneral"));
            log.info("Código generado recibido del microservicio: {}", codigoResponse);

            Almacen nuevoAlmacen = new Almacen();
            nuevoAlmacen.setNumeroSeguimiento(codigoResponse);
            nuevoAlmacen.setUbicacion(almacenRequest.getUbicacion());
            nuevoAlmacen.setCapacidad(almacenRequest.getCapacidad());
            
            // Inicializar las listas como vacías para evitar null
            nuevoAlmacen.setHerramientas(new ArrayList<>());
            nuevoAlmacen.setMateriasPrimas(new ArrayList<>());
            nuevoAlmacen.setMedicamentos(new ArrayList<>());

            Almacen almacenGuardado = almacenRepository.save(nuevoAlmacen);
            log.info("Almacén guardado correctamente con ID: {}", almacenGuardado.getId());

            return new CodigoResponse<>(200, "Almacén guardado correctamente", mapToResponse(almacenGuardado));

        } catch (Exception e) {
            log.error("Error al guardar el almacén: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al guardar el almacén: " + e.getMessage(), null);
        }
    }

    // Obtener todos
    public CodigoResponse<List<AlmacenResponse>> obtenerTodos() {
        List<Almacen> almacenes = almacenRepository.findAll();
        List<AlmacenResponse> lista = almacenes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return new CodigoResponse<>(200, "Lista de almacenes obtenida", lista);
    }

    // Obtener por ID
    public CodigoResponse<AlmacenResponse> obtenerPorId(Long id) {
        Optional<Almacen> opt = almacenRepository.findById(id);
        if (opt.isPresent()) {
            return new CodigoResponse<>(200, "Almacén encontrado", mapToResponse(opt.get()));
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

    // Mapeo de entidad a Response (ÚNICO método mapToResponse)
    private AlmacenResponse mapToResponse(Almacen a) {
        AlmacenResponse response = new AlmacenResponse();
        response.setId(a.getId());
        response.setNumeroSeguimiento(a.getNumeroSeguimiento());
        response.setUbicacion(a.getUbicacion());
        response.setCapacidad(a.getCapacidad());

        // Materias primas - manejar lista nula
        List<MateriasPrimasResponse> materias = a.getMateriasPrimas() != null 
            ? a.getMateriasPrimas().stream()
                    .map(m -> new MateriasPrimasResponse(m.getId(), m.getNombre(), m.getFoto(),
                            m.getCantidad(), m.getIdProveedor()))
                    .collect(Collectors.toList())
            : new ArrayList<>();
        response.setMateriasPrimas(materias);

        // Herramientas - manejar lista nula
        List<HerramientasResponse> herramientas = a.getHerramientas() != null
                ? a.getHerramientas().stream()
                        .map(h -> new HerramientasResponse(h.getId(), h.getNombre(),
                                h.getFoto(), h.getIdProveedor()))
                        .collect(Collectors.toList())
                : new ArrayList<>();
        response.setHerramientas(herramientas);

        // Medicamentos - manejar lista nula
        List<MedicamentosResponse> medicamentos = a.getMedicamentos() != null
                ? a.getMedicamentos().stream()
                        .map(m -> new MedicamentosResponse(m.getId(), m.getNombre(),
                                m.getCantidad(), m.getDescripcion(), m.getFoto(), m.getIdProveedor()))
                        .collect(Collectors.toList())
                : new ArrayList<>();
        response.setMedicamentos(medicamentos);

        return response;
    }
}