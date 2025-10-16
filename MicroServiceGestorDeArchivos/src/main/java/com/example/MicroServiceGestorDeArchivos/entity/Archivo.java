package com.example.MicroServiceGestorDeArchivos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "archivos")
public class Archivo {
    @Id
    private String id; 

    @Column(nullable = false)
    private String nombreOriginal;

    @Column(nullable = false)
    private String nombreAlmacenado;

    @Column(nullable = false)
    private String rutaCompleta;

    @Column(nullable = false)
    private String tipoMime;

    private Long tamaño;

    // Metadata para relación con otras entidades
    @Column(name = "servicio_origen", nullable = false)
    private String servicioOrigen; 

    @Column(name = "entidad_origen", nullable = false)
    private String entidadOrigen; 

    @Column(name = "entidad_id", nullable = false)
    private String entidadId; 

    @Column(name = "campo_asociado")
    private String campoAsociado; 

    // Metadata para imágenes
    private Integer ancho;
    private Integer alto;
    private String resolucion;

    private String hash;

    @CreationTimestamp
    private LocalDateTime fechaSubida;

    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean activo = true;
}