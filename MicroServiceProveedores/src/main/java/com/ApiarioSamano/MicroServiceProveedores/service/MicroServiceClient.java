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

    // ✅ Subir un archivo al microservicio de archivos
    public ArchivoDTO uploadFile(File file, String servicioOrigen, String entidadOrigen, String entidadId, String campoAsociado) {
        String url = fileServiceUrl + "/upload";
        log.info("Subiendo archivo a: {}", url);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));
        body.add("servicioOrigen", servicioOrigen);
        body.add("entidadOrigen", entidadOrigen);
        body.add("entidadId", entidadId);
        body.add("campoAsociado", campoAsociado);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<ArchivoDTO> response = restTemplate.postForEntity(url, requestEntity, ArchivoDTO.class);
        return response.getBody();
    }

    // ✅ Obtener lista de archivos (metadatos)
    public List<ArchivoDTO> getFilesByEntity(String servicioOrigen, String entidadOrigen, String entidadId) {
        String url = String.format("%s/entidad/%s/%s/%s", fileServiceUrl, servicioOrigen, entidadOrigen, entidadId);
        log.info("Obteniendo archivos de entidad: {}", url);

        ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
        return response.getBody();
    }

    // ✅ Obtener archivo por campo (metadatos)
    public ArchivoDTO getFileByField(String servicioOrigen, String entidadOrigen, String entidadId, String campoAsociado) {
        String url = String.format("%s/entidad/%s/%s/%s/%s", fileServiceUrl, servicioOrigen, entidadOrigen, entidadId, campoAsociado);
        log.info("Obteniendo archivo por campo: {}", url);

        ResponseEntity<ArchivoDTO> response = restTemplate.getForEntity(url, ArchivoDTO.class);
        return response.getBody();
    }

    // ✅ Descargar archivo real (Resource)
    public Resource downloadFile(String fileId) {
        String url = fileServiceUrl + "/download/" + fileId;
        log.info("Descargando archivo: {}", url);

        ResponseEntity<Resource> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                Resource.class
        );

        return response.getBody();
    }

    // ✅ Visualizar archivo real (Resource)
    public Resource viewFile(String fileId) {
        String url = fileServiceUrl + "/view/" + fileId;
        log.info("Visualizando archivo: {}", url);

        ResponseEntity<Resource> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                Resource.class
        );

        return response.getBody();
    }
}
