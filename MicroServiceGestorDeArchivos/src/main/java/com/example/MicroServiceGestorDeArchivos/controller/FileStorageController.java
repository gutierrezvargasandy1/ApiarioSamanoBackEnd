package com.example.MicroServiceGestorDeArchivos.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.MicroServiceGestorDeArchivos.dto.FileUploadRequest;
import com.example.MicroServiceGestorDeArchivos.dto.FileUploadResponse;
import com.example.MicroServiceGestorDeArchivos.entity.Archivo;
import com.example.MicroServiceGestorDeArchivos.service.FileStorageService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileStorageController {

    private final FileStorageService fileStorageService;

    public FileStorageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("servicioOrigen") String servicioOrigen,
            @RequestParam("entidadOrigen") String entidadOrigen,
            @RequestParam("entidadId") String entidadId,
            @RequestParam(value = "campoAsociado", required = false) String campoAsociado) {

        FileUploadRequest request = new FileUploadRequest(servicioOrigen, entidadOrigen, entidadId, campoAsociado);
        
        try {
            FileUploadResponse response = fileStorageService.guardarArchivo(file, request);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        try {
            return fileStorageService.descargarArchivo(fileId);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/view/{fileId}")
    public ResponseEntity<Resource> viewFile(@PathVariable String fileId) {
        try {
            return fileStorageService.visualizarArchivo(fileId);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/entidad/{servicioOrigen}/{entidadOrigen}/{entidadId}")
    public ResponseEntity<List<Archivo>> getFilesByEntity(
            @PathVariable String servicioOrigen,
            @PathVariable String entidadOrigen,
            @PathVariable String entidadId) {
        
        List<Archivo> archivos = fileStorageService.obtenerArchivosPorEntidad(
                servicioOrigen, entidadOrigen, entidadId);
        return ResponseEntity.ok(archivos);
    }

    @GetMapping("/entidad/{servicioOrigen}/{entidadOrigen}/{entidadId}/{campoAsociado}")
    public ResponseEntity<Archivo> getFileByField(
            @PathVariable String servicioOrigen,
            @PathVariable String entidadOrigen,
            @PathVariable String entidadId,
            @PathVariable String campoAsociado) {
        
        Archivo archivo = fileStorageService.obtenerArchivoPorCampo(
                servicioOrigen, entidadOrigen, entidadId, campoAsociado);
        
        if (archivo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(archivo);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileId) {
        try {
            fileStorageService.eliminarArchivo(fileId);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}