package com.ApiarioSamano.MicroServiceProveedores.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
    private int codigo;
    private String descripcion;
    private T data;
    private LocalDateTime timestamp;
    private String mensaje;

    public static <T> ApiResponse<T> success(T data, String descripcion) {
        return ApiResponse.<T>builder()
                .codigo(200)
                .descripcion(descripcion)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> created(T data, String descripcion) {
        return ApiResponse.<T>builder()
                .codigo(201)
                .descripcion(descripcion)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(CodigoEstatus estatus, String mensaje) {
        return ApiResponse.<T>builder()
                .codigo(estatus.getCodigo())
                .descripcion(estatus.getDescripcion())
                .mensaje(mensaje)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> notFound(String mensaje) {
        return ApiResponse.<T>builder()
                .codigo(404)
                .descripcion(CodigoEstatus.NO_ENCONTRADO.getDescripcion())
                .mensaje(mensaje)
                .timestamp(LocalDateTime.now())
                .build();
    }
}