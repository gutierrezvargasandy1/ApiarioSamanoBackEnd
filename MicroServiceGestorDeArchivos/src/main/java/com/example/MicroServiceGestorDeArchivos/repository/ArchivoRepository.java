package com.example.MicroServiceGestorDeArchivos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.MicroServiceGestorDeArchivos.entity.Archivo;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArchivoRepository extends JpaRepository<Archivo, String> {
    
    // Buscar archivos por entidad específica
    List<Archivo> findByServicioOrigenAndEntidadOrigenAndEntidadIdAndActivoTrue(
            String servicioOrigen, String entidadOrigen, String entidadId);
    
    // Buscar archivo específico por campo asociado
    Optional<Archivo> findByServicioOrigenAndEntidadOrigenAndEntidadIdAndCampoAsociadoAndActivoTrue(
            String servicioOrigen, String entidadOrigen, String entidadId, String campoAsociado);
    
    // Verificar si existe archivo para una entidad
    boolean existsByServicioOrigenAndEntidadOrigenAndEntidadIdAndActivoTrue(
            String servicioOrigen, String entidadOrigen, String entidadId);
    
    // Obtener todos los archivos de un servicio
    @Query("SELECT a FROM Archivo a WHERE a.servicioOrigen = :servicio AND a.activo = true")
    List<Archivo> findByServicioOrigen(@Param("servicio") String servicioOrigen);
}