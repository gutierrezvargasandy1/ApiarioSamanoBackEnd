package com.ApiarioSamano.MicroServiceProveedores.service;

import com.ApiarioSamano.MicroServiceProveedores.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorDTO.ProveedorRequest;
import com.ApiarioSamano.MicroServiceProveedores.model.Proveedor;
import com.ApiarioSamano.MicroServiceProveedores.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    public CodigoResponse<List<Proveedor>> obtenerTodos() {
        List<Proveedor> proveedores = proveedorRepository.findAll();
        return new CodigoResponse<>(200, "Lista de proveedores obtenida correctamente", proveedores);
    }

    public CodigoResponse<Proveedor> obtenerPorId(Long id) {
        Optional<Proveedor> proveedor = proveedorRepository.findById(id);
        if (proveedor.isPresent()) {
            return new CodigoResponse<>(200, "Proveedor encontrado", proveedor.get());
        }
        return new CodigoResponse<>(404, "Proveedor no encontrado", null);
    }

    public CodigoResponse<Proveedor> guardarProveedor(ProveedorRequest request) {
        Proveedor proveedor = new Proveedor();
        proveedor.setFotografia(request.getFotografia());
        proveedor.setNombreEmpresa(request.getNombreEmpresa());
        proveedor.setNombreRepresentante(request.getNombreReprecentante());
        proveedor.setNumTelefono(request.getNumTelefono());
        proveedor.setMaterialProvee(request.getMaterialProvee());

        Proveedor guardado = proveedorRepository.save(proveedor);
        return new CodigoResponse<>(201, "Proveedor registrado correctamente", guardado);
    }

    public CodigoResponse<Proveedor> actualizarProveedor(Long id, ProveedorRequest request) {
        Optional<Proveedor> proveedorExistente = proveedorRepository.findById(id);
        if (proveedorExistente.isPresent()) {
            Proveedor proveedor = proveedorExistente.get();
            proveedor.setFotografia(request.getFotografia());
            proveedor.setNombreEmpresa(request.getNombreEmpresa());
            proveedor.setNombreRepresentante(request.getNombreReprecentante());
            proveedor.setNumTelefono(request.getNumTelefono());
            proveedor.setMaterialProvee(request.getMaterialProvee());
            Proveedor actualizado = proveedorRepository.save(proveedor);
            return new CodigoResponse<>(200, "Proveedor actualizado correctamente", actualizado);
        }
        return new CodigoResponse<>(404, "Proveedor no encontrado", null);
    }

    public CodigoResponse<Void> eliminarProveedor(Long id) {
        if (proveedorRepository.existsById(id)) {
            proveedorRepository.deleteById(id);
            return new CodigoResponse<>(200, "Proveedor eliminado correctamente", null);
        }
        return new CodigoResponse<>(404, "Proveedor no encontrado", null);
    }
}
