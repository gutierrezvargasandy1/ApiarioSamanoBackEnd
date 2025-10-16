package com.ApiarioSamano.MicroServiceProveedores.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Proveedores")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String fotografia;

    @Column(name = "nombre_empresa", nullable = false, length = 200)
    private String nombreEmpresa;

    @Column(name = "nombre-Reprecentante", nullable = false, length = 200)
    private String nombreReprecentante;

    @Column(name = "num_telefono", length = 20)
    private String numTelefono;

    @Column(name = "material_provee", length = 200)
    private String materialProvee;
}
