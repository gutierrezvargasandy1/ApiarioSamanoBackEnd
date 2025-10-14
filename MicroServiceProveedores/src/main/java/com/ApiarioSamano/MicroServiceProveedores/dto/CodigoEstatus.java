package com.ApiarioSamano.MicroServiceProveedores.dto;

public enum CodigoEstatus {
    EXITOSO(200, "Operación exitosa"),
    CREADO(201, "Recurso creado exitosamente"),
    SOLICITUD_INCORRECTA(400, "Solicitud incorrecta"),
    NO_AUTORIZADO(401, "No autorizado"),
    PROHIBIDO(403, "Acceso prohibido"),
    NO_ENCONTRADO(404, "Recurso no encontrado"),
    CONFLICTO(409, "Conflicto en la operación"),
    ERROR_INTERNO(500, "Error interno del servidor");

    private final int codigo;
    private final String descripcion;

    CodigoEstatus(int codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }
}