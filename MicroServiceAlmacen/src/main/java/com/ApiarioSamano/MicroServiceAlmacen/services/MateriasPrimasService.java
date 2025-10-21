package com.ApiarioSamano.MicroServiceAlmacen.services;

import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.AlmacenDTO.AlmacenResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO.MateriasPrimasConProveedorDTO;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO.MateriasPrimasRequest;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO.MateriasPrimasResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.ProveedoresClientMicroserviceDTO.ProveedorResponseDTO;
import com.ApiarioSamano.MicroServiceAlmacen.model.MateriasPrimas;
import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import com.ApiarioSamano.MicroServiceAlmacen.repository.AlmacenRepository;
import com.ApiarioSamano.MicroServiceAlmacen.repository.MateriasPrimasRepository;
import com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI.ProveedoresClientMicroservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MateriasPrimasService {

    private final MateriasPrimasRepository materiasPrimasRepository;
    private final ProveedoresClientMicroservice proveedoresClient;
    private final AlmacenRepository almacenRepository;

    private AlmacenResponse mapAlmacen(Almacen almacen) {
        if (almacen == null)
            return null;

        AlmacenResponse response = new AlmacenResponse();
        response.setId(almacen.getId());
        response.setNumeroSeguimiento(almacen.getNumeroSeguimiento());
        response.setUbicacion(almacen.getUbicacion());
        response.setCapacidad(almacen.getCapacidad());

        return response;
    }

    private MateriasPrimasResponse mapMateria(MateriasPrimas m) {
        MateriasPrimasResponse response = new MateriasPrimasResponse();
        response.setId(m.getId());
        response.setNombre(m.getNombre());
        response.setFoto(m.getFoto());
        response.setCantidad(m.getCantidad());
        response.setIdProveedor(m.getIdProveedor());

        return response;
    }

    public CodigoResponse<MateriasPrimasResponse> guardar(MateriasPrimasRequest mp) {
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();
        boolean existeProveedor = proveedores.stream()
                .anyMatch(p -> p.getId().equals(mp.getIdProvedor().longValue()));

        if (!existeProveedor) {
            return new CodigoResponse<>(404, "Proveedor no encontrado", null);
        }

        Almacen almacen = almacenRepository.findById(mp.getIdAlmacen())
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));

        MateriasPrimas materia = new MateriasPrimas();
        materia.setNombre(mp.getNombre());
        materia.setFoto(mp.getFoto());
        materia.setCantidad(mp.getCantidad());
        materia.setAlmacen(almacen);
        materia.setIdProveedor(mp.getIdProvedor());

        MateriasPrimas guardado = materiasPrimasRepository.save(materia);

        return new CodigoResponse<>(200, "Materia prima guardada correctamente", mapMateria(guardado));
    }

    public CodigoResponse<List<MateriasPrimasResponse>> obtenerTodas() {
        List<MateriasPrimasResponse> lista = materiasPrimasRepository.findAll()
                .stream()
                .map(this::mapMateria)
                .collect(Collectors.toList());
        return new CodigoResponse<>(200, "Lista de materias primas obtenida", lista);
    }

    public CodigoResponse<List<MateriasPrimasConProveedorDTO>> obtenerTodasConProveedor() {
        List<MateriasPrimas> materias = materiasPrimasRepository.findAll();
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();

        List<MateriasPrimasConProveedorDTO> resultado = materias.stream().map(m -> {
            MateriasPrimasConProveedorDTO dto = new MateriasPrimasConProveedorDTO();
            dto.setId(m.getId());
            dto.setNombre(m.getNombre());
            dto.setFoto(m.getFoto());
            dto.setCantidad(m.getCantidad());
            dto.setAlmacen(mapAlmacen(m.getAlmacen()));
            proveedores.stream()
                    .filter(p -> p.getId().equals(m.getIdProveedor().longValue()))
                    .findFirst()
                    .ifPresent(dto::setProveedor);
            return dto;
        }).collect(Collectors.toList());

        return new CodigoResponse<>(200, "Materias primas con proveedor obtenidas", resultado);
    }

    public CodigoResponse<MateriasPrimasConProveedorDTO> obtenerPorIdConProveedor(Long id) {
        Optional<MateriasPrimas> optMateria = materiasPrimasRepository.findById(id);
        if (optMateria.isEmpty()) {
            return new CodigoResponse<>(404, "Materia prima no encontrada", null);
        }

        MateriasPrimas materia = optMateria.get();
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();

        MateriasPrimasConProveedorDTO dto = new MateriasPrimasConProveedorDTO();
        dto.setId(materia.getId());
        dto.setNombre(materia.getNombre());
        dto.setFoto(materia.getFoto());
        dto.setCantidad(materia.getCantidad());
        dto.setAlmacen(mapAlmacen(materia.getAlmacen()));
        proveedores.stream()
                .filter(p -> p.getId().equals(materia.getIdProveedor().longValue()))
                .findFirst()
                .ifPresent(dto::setProveedor);

        return new CodigoResponse<>(200, "Materia prima con proveedor obtenida", dto);
    }

    public CodigoResponse<MateriasPrimasResponse> obtenerPorId(Long id) {
        Optional<MateriasPrimas> opt = materiasPrimasRepository.findById(id);
        return opt.map(m -> new CodigoResponse<>(200, "Materia prima encontrada", mapMateria(m)))
                .orElseGet(() -> new CodigoResponse<>(404, "Materia prima no encontrada", null));
    }

    public CodigoResponse<List<MateriasPrimasResponse>> obtenerPorAlmacen(Almacen almacen) {
        List<MateriasPrimasResponse> lista = materiasPrimasRepository.findByAlmacen(almacen)
                .stream()
                .map(this::mapMateria)
                .collect(Collectors.toList());
        return new CodigoResponse<>(200, "Materias primas del almacén obtenidas", lista);
    }

    public CodigoResponse<List<MateriasPrimasResponse>> obtenerPorProveedor(Integer idProveedor) {
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();
        boolean existeProveedor = proveedores.stream()
                .anyMatch(p -> p.getId().equals(idProveedor.longValue()));

        if (!existeProveedor) {
            return new CodigoResponse<>(404, "Proveedor no encontrado", List.of());
        }

        List<MateriasPrimasResponse> lista = materiasPrimasRepository.findByIdProveedor(idProveedor)
                .stream()
                .map(this::mapMateria)
                .collect(Collectors.toList());
        return new CodigoResponse<>(200, "Materias primas del proveedor obtenidas", lista);
    }

    public CodigoResponse<Void> eliminarPorId(Long id) {
        if (materiasPrimasRepository.existsById(id)) {
            materiasPrimasRepository.deleteById(id);
            return new CodigoResponse<>(200, "Materia prima eliminada correctamente", null);
        }
        return new CodigoResponse<>(404, "Materia prima no encontrada", null);
    }
}
