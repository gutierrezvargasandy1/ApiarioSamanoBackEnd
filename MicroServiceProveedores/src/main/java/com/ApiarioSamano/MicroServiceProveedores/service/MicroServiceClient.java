package com.ApiarioSamano.MicroServiceProveedores.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.ApiarioSamano.MicroServiceProveedores.dto.ArchivoDTO;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class MicroServiceClient {

    private static final Logger log = LoggerFactory.getLogger(MicroServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${microservicio.archivos.url}")
    private String fileServiceUrl;

    @Value("${microservicio.archivos.jwt}")
    private String jwtToken;

    @Value("${microservicio.generador.url}")
    private String generadorCodigoUrl;

    /**
     * 🔹 Genera los headers con el JWT incluido
     */
    private HttpHeaders createHeaders(MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        if (contentType != null) {
            headers.setContentType(contentType);
        }
        headers.setBearerAuth(jwtToken);
        return headers;
    }

    public ArchivoDTO uploadFile(File file, String servicioOrigen, String entidadOrigen, String entidadId,
            String campoAsociado) {
        String url = fileServiceUrl + "/upload";
        log.info("Subiendo archivo a: {}", url);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));
        body.add("servicioOrigen", servicioOrigen);
        body.add("entidadOrigen", entidadOrigen);
        body.add("entidadId", entidadId);
        body.add("campoAsociado", campoAsociado);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body,
                createHeaders(MediaType.MULTIPART_FORM_DATA));

        ResponseEntity<ArchivoDTO> response = restTemplate.postForEntity(url, requestEntity, ArchivoDTO.class);
        return response.getBody();
    }

    public List<ArchivoDTO> getFilesByEntity(String servicioOrigen, String entidadOrigen, String entidadId) {
        String url = String.format("%s/entidad/%s/%s/%s", fileServiceUrl, servicioOrigen, entidadOrigen, entidadId);
        log.info("Obteniendo archivos de entidad: {}", url);

        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders(null));
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, List.class);
        return response.getBody();
    }

    public ArchivoDTO getFileByField(String servicioOrigen, String entidadOrigen, String entidadId,
            String campoAsociado) {
        String url = String.format("%s/entidad/%s/%s/%s/%s", fileServiceUrl, servicioOrigen, entidadOrigen, entidadId,
                campoAsociado);
        log.info("Obteniendo archivo por campo: {}", url);

        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders(null));
        ResponseEntity<ArchivoDTO> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity,
                ArchivoDTO.class);
        return response.getBody();
    }

    public Resource downloadFile(String fileId) {
        String url = fileServiceUrl + "/download/" + fileId;
        log.info("Descargando archivo: {}", url);

        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders(null));
        ResponseEntity<Resource> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Resource.class);
        return response.getBody();
    }

    public Resource viewFile(String fileId) {
        String url = fileServiceUrl + "/view/" + fileId;
        log.info("Visualizando archivo: {}", url);

        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders(null));
        ResponseEntity<Resource> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Resource.class);
        return response.getBody();
    }

    public String generarIdArchivo() {
        String url = generadorCodigoUrl + "/api/codigos/archivo";
        log.info("Solicitando ID de archivo a: {}", url);

        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders(null));

        try {
            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Object.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Extrae el campo "codigo" del JSON recibido
                var responseBody = (java.util.Map<String, Object>) response.getBody();
                return (String) responseBody.get("codigo");
            } else {
                log.error("Error al generar ID de archivo. Respuesta: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error al comunicarse con el microservicio generador de códigos: {}", e.getMessage());
            return null;
        }
    }
}
