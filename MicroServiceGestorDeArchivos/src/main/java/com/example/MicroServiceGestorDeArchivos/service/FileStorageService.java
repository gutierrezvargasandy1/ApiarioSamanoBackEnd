package com.example.MicroServiceGestorDeArchivos.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.MicroServiceGestorDeArchivos.dto.FileUploadRequest;
import com.example.MicroServiceGestorDeArchivos.dto.FileUploadResponse;
import com.example.MicroServiceGestorDeArchivos.entity.Archivo;
import com.example.MicroServiceGestorDeArchivos.repository.ArchivoRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.storage.local.directory}")
    private String storageDirectory;

    @Value("${app.storage.max-file-size}") // 10MB default
    private long maxFileSize;

    @Value("${app.server.url}")
    private String serverUrl;

    private final ArchivoRepository archivoRepository;

    public FileStorageService(ArchivoRepository archivoRepository) {
        this.archivoRepository = archivoRepository;
    }

    public FileUploadResponse guardarArchivo(MultipartFile archivo, FileUploadRequest request) throws IOException {
        // Validaciones
        validarArchivo(archivo);

        // Generar ID único y nombre seguro
        String fileId = UUID.randomUUID().toString();
        String extension = obtenerExtension(archivo.getOriginalFilename());
        String nombreAlmacenado = fileId + "." + extension;
        String rutaCompleta = storageDirectory + File.separator + nombreAlmacenado;

        // Crear directorios si no existen
        crearDirectorios();

        // Guardar archivo físicamente
        Path destino = Paths.get(rutaCompleta);
        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        // Extraer metadata
        Metadata metadata = extraerMetadata(archivo, rutaCompleta);

        // Guardar en base de datos
        Archivo archivoEntity = new Archivo();
        archivoEntity.setId(fileId);
        archivoEntity.setNombreOriginal(archivo.getOriginalFilename());
        archivoEntity.setNombreAlmacenado(nombreAlmacenado);
        archivoEntity.setRutaCompleta(rutaCompleta);
        archivoEntity.setTipoMime(archivo.getContentType());
        archivoEntity.setTamaño(archivo.getSize());
        archivoEntity.setServicioOrigen(request.getServicioOrigen());
        archivoEntity.setEntidadOrigen(request.getEntidadOrigen());
        archivoEntity.setEntidadId(request.getEntidadId());
        archivoEntity.setCampoAsociado(request.getCampoAsociado());
        archivoEntity.setHash(calcularHash(archivo));
        archivoEntity.setFechaSubida(LocalDateTime.now());

        // Metadata para imágenes
        if (metadata.esImagen) {
            archivoEntity.setAncho(metadata.ancho);
            archivoEntity.setAlto(metadata.alto);
        }

        archivoRepository.save(archivoEntity);

        // Construir respuesta
        return new FileUploadResponse(
                fileId,
                archivo.getOriginalFilename(),
                serverUrl + "/api/files/download/" + fileId,
                serverUrl + "/api/files/view/" + fileId,
                archivo.getSize(),
                archivo.getContentType(),
                metadata.ancho,
                metadata.alto,
                LocalDateTime.now()
        );
    }

    public ResponseEntity<Resource> descargarArchivo(String fileId) throws IOException {
        Archivo archivo = archivoRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));

        Path rutaArchivo = Paths.get(archivo.getRutaCompleta());
        Resource resource = new UrlResource(rutaArchivo.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("No se puede leer el archivo: " + archivo.getNombreAlmacenado());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.getTipoMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + archivo.getNombreOriginal() + "\"")
                .body(resource);
    }

    public ResponseEntity<Resource> visualizarArchivo(String fileId) throws IOException {
        Archivo archivo = archivoRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));

        Path rutaArchivo = Paths.get(archivo.getRutaCompleta());
        Resource resource = new UrlResource(rutaArchivo.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("No se puede leer el archivo: " + archivo.getNombreAlmacenado());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.getTipoMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "inline; filename=\"" + archivo.getNombreOriginal() + "\"")
                .body(resource);
    }

    public List<Archivo> obtenerArchivosPorEntidad(String servicioOrigen, String entidadOrigen, String entidadId) {
        return archivoRepository.findByServicioOrigenAndEntidadOrigenAndEntidadIdAndActivoTrue(
                servicioOrigen, entidadOrigen, entidadId);
    }

    public Archivo obtenerArchivoPorCampo(String servicioOrigen, String entidadOrigen, 
                                         String entidadId, String campoAsociado) {
        return archivoRepository
                .findByServicioOrigenAndEntidadOrigenAndEntidadIdAndCampoAsociadoAndActivoTrue(
                        servicioOrigen, entidadOrigen, entidadId, campoAsociado)
                .orElse(null);
    }

    public void eliminarArchivo(String fileId) throws IOException {
        Archivo archivo = archivoRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));

        // Eliminar archivo físico
        Files.deleteIfExists(Paths.get(archivo.getRutaCompleta()));

        // Eliminar lógicamente de la BD
        archivo.setActivo(false);
        archivoRepository.save(archivo);
    }

    // Métodos auxiliares privados
    private void validarArchivo(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new RuntimeException("El archivo está vacío");
        }
        if (archivo.getSize() > maxFileSize) {
            throw new RuntimeException("El archivo excede el tamaño máximo permitido");
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        return nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();
    }

    private void crearDirectorios() throws IOException {
        Path directorio = Paths.get(storageDirectory);
        if (!Files.exists(directorio)) {
            Files.createDirectories(directorio);
        }
    }

    private Metadata extraerMetadata(MultipartFile archivo, String ruta) {
        Metadata metadata = new Metadata();
        
        if (archivo.getContentType() != null && archivo.getContentType().startsWith("image/")) {
            try {
                BufferedImage imagen = ImageIO.read(new File(ruta));
                if (imagen != null) {
                    metadata.esImagen = true;
                    metadata.ancho = imagen.getWidth();
                    metadata.alto = imagen.getHeight();
                }
            } catch (IOException e) {
                log.warn("No se pudo extraer metadata de la imagen: {}", e.getMessage());
            }
        }
        
        return metadata;
    }

    private String calcularHash(MultipartFile archivo) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(archivo.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al calcular hash del archivo", e);
        }
    }

    private static class Metadata {
        boolean esImagen = false;
        Integer ancho = null;
        Integer alto = null;
    }
}