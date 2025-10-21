package com.ApiarioSamano.MicroServiceAlmacen.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.ApiarioSamano.MicroServiceAlmacen.model.Medicamento;
import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import com.ApiarioSamano.MicroServiceAlmacen.repository.AlmacenRepository;
import com.ApiarioSamano.MicroServiceAlmacen.repository.MedicamentoRepository;
import com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI.ProveedoresClientMicroservice;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MedicamentosDTO.MedicamentosRequest;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MedicamentosDTO.MedicamentosResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MedicamentosDTO.MedicamnetosConProveedorResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.ProveedoresClientMicroserviceDTO.ProveedorResponseDTO;

@Service
public class MedicamentosService {

    @Autowired
    private MedicamentoRepository medicamentosRepository;

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private ProveedoresClientMicroservice proveedorClient;

    // 📌 Crear o actualizar medicamento
    public MedicamentosResponse guardar(MedicamentosRequest request) {
        Medicamento medicamento = new Medicamento();

        medicamento.setNombre(request.getNombre());
        medicamento.setDescripcion(request.getDescripcion());
        medicamento.setIdProveedor(request.getIdProveedor());

        if (request.getIdAlmacen() != null) {
            Optional<Almacen> almacenOpt = almacenRepository.findById(Long.valueOf(request.getIdAlmacen()));
            almacenOpt.ifPresent(medicamento::setAlmacen);
        }

        Medicamento guardado = medicamentosRepository.save(medicamento);
        return mapToResponse(guardado);
    }

    // En tu MedicamentosService del microservicio de almacén
    public MedicamentosResponse obtenerPorId(Long id) {
        Medicamento medicamento = medicamentosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado con ID: " + id));

        // ✅ CONVERSIÓN MANUAL DIRECTAMENTE EN EL MÉTODO
        MedicamentosResponse response = new MedicamentosResponse();
        response.setId(medicamento.getId());
        response.setNombre(medicamento.getNombre());
        response.setDescripcion(medicamento.getDescripcion());
        response.setCantidad(medicamento.getCantidad());
        response.setIdProveedor(medicamento.getIdProveedor());
        response.setFoto(medicamento.getFoto()); // byte[] foto

        // Si el almacen no es null, puedes setear su ID o información básica
        if (medicamento.getAlmacen() != null) {
            // Dependiendo de tu DTO, puedes setear el ID del almacén o crear un objeto
            // simple
            // response.setIdAlmacen(medicamento.getAlmacen().getId());
            // response.setNombreAlmacen(medicamento.getAlmacen().getNombre());
        }

        return response;
    }

    // 📌 Obtener todos los medicamentos (sin proveedor)
    public List<MedicamentosResponse> obtenerTodos() {
        return medicamentosRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 📌 Obtener todos los medicamentos con su proveedor (consulta al microservicio
    // Proveedores)
    public List<MedicamnetosConProveedorResponse> obtenerTodosConProveedor() {
        return medicamentosRepository.findAll()
                .stream()
                .map(this::mapToResponseConProveedor)
                .collect(Collectors.toList());
    }

    // 📌 Obtener medicamentos por ID de proveedor
    public List<MedicamentosResponse> obtenerPorProveedor(Integer idProveedor) {
        return medicamentosRepository.findByIdProveedor(idProveedor)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 📌 Obtener medicamentos por ID de proveedor (con datos del proveedor)
    public List<MedicamnetosConProveedorResponse> obtenerPorProveedorConDetalle(Integer idProveedor) {
        return medicamentosRepository.findByIdProveedor(idProveedor)
                .stream()
                .map(this::mapToResponseConProveedor)
                .collect(Collectors.toList());
    }

    // 📌 Eliminar medicamento
    public void eliminar(Long id) {
        medicamentosRepository.deleteById(id);
    }

    // ==========================
    // MÉTODOS DE MAPEOS
    // ==========================
    private MedicamentosResponse mapToResponse(Medicamento m) {
        MedicamentosResponse response = new MedicamentosResponse();
        response.setId(m.getId());
        response.setNombre(m.getNombre());
        response.setDescripcion(m.getDescripcion());
        response.setCantidad(m.getCantidad());
        response.setFoto(m.getFoto());
        response.setIdProveedor(m.getIdProveedor());
        return response;
    }

    private MedicamnetosConProveedorResponse mapToResponseConProveedor(Medicamento m) {
        MedicamnetosConProveedorResponse response = new MedicamnetosConProveedorResponse();
        response.setId(m.getId());
        response.setNombre(m.getNombre());
        response.setDescripcion(m.getDescripcion());
        response.setCantidad(m.getCantidad());
        response.setFoto(m.getFoto());

        try {
            // Obtener todos los proveedores desde el microservicio
            List<ProveedorResponseDTO> proveedores = proveedorClient.obtenerTodosProveedores();

            // Buscar el proveedor correspondiente al idProveedor del medicamento
            ProveedorResponseDTO proveedor = proveedores.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(m.getIdProveedor()))
                    .findFirst()
                    .orElse(null);

            response.setProveedor(proveedor);
        } catch (Exception e) {
            // En caso de error en la comunicación con el microservicio
            response.setProveedor(null);
            System.err
                    .println("⚠️ Error al obtener proveedor para medicamento ID " + m.getId() + ": " + e.getMessage());
        }

        return response;
    }

}
