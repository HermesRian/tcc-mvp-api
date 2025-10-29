package com.petapp.petapp.controller;

import com.petapp.petapp.entity.PetInfo;
import com.petapp.petapp.repository.PetInfoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pet-info")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pet Info", description = "Endpoints para buscar informações estruturadas dos pets")
public class PetInfoController {

    private final PetInfoRepository petInfoRepository;

    @Operation(summary = "Listar todas as informações estruturadas",
               description = "Retorna todas as informações de pets extraídas e estruturadas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Informações retornadas com sucesso",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = PetInfo.class))),
        @ApiResponse(responseCode = "500", description = "Erro ao buscar informações")
    })
    @GetMapping
    public ResponseEntity<List<PetInfo>> getAllPetInfo() {
        log.info("Received request to get all pet info");
        try {
            List<PetInfo> petInfoList = petInfoRepository.findAll();
            return ResponseEntity.ok(petInfoList);
        } catch (Exception e) {
            log.error("Error fetching pet info", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Buscar por tipo de pet",
               description = "Retorna informações filtradas por tipo de pet (ex: Gato, Cachorro)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Informações retornadas com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro ao buscar informações")
    })
    @GetMapping("/type/{petType}")
    public ResponseEntity<List<PetInfo>> getByPetType(
            @Parameter(description = "Tipo do pet (ex: Gato, Cachorro)", required = true)
            @PathVariable String petType) {
        log.info("Received request to get pet info by type: {}", petType);
        try {
            List<PetInfo> petInfoList = petInfoRepository.findByPetType(petType);
            return ResponseEntity.ok(petInfoList);
        } catch (Exception e) {
            log.error("Error fetching pet info by type", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Buscar por status",
               description = "Retorna informações filtradas por status (ex: Desaparecido, Encontrado)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Informações retornadas com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro ao buscar informações")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PetInfo>> getByStatus(
            @Parameter(description = "Status do pet (ex: Desaparecido, Encontrado)", required = true)
            @PathVariable String status) {
        log.info("Received request to get pet info by status: {}", status);
        try {
            List<PetInfo> petInfoList = petInfoRepository.findByPetStatus(status);
            return ResponseEntity.ok(petInfoList);
        } catch (Exception e) {
            log.error("Error fetching pet info by status", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Buscar por localização",
               description = "Retorna informações filtradas por localização (busca parcial, case-insensitive)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Informações retornadas com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro ao buscar informações")
    })
    @GetMapping("/location/{location}")
    public ResponseEntity<List<PetInfo>> getByLocation(
            @Parameter(description = "Localização (busca parcial)", required = true)
            @PathVariable String location) {
        log.info("Received request to get pet info by location: {}", location);
        try {
            List<PetInfo> petInfoList = petInfoRepository.findByLocationContainingIgnoreCase(location);
            return ResponseEntity.ok(petInfoList);
        } catch (Exception e) {
            log.error("Error fetching pet info by location", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Buscar por data do evento",
               description = "Retorna informações filtradas por data do evento (formato: YYYY-MM-DD)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Informações retornadas com sucesso"),
        @ApiResponse(responseCode = "400", description = "Data inválida"),
        @ApiResponse(responseCode = "500", description = "Erro ao buscar informações")
    })
    @GetMapping("/date/{date}")
    public ResponseEntity<List<PetInfo>> getByDate(
            @Parameter(description = "Data do evento (formato: YYYY-MM-DD)", required = true)
            @PathVariable String date) {
        log.info("Received request to get pet info by date: {}", date);
        try {
            LocalDate eventDate = LocalDate.parse(date);
            List<PetInfo> petInfoList = petInfoRepository.findByEventDate(eventDate);
            return ResponseEntity.ok(petInfoList);
        } catch (Exception e) {
            log.error("Error fetching pet info by date", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Buscar por tipo e status",
               description = "Retorna informações filtradas por tipo de pet e status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Informações retornadas com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro ao buscar informações")
    })
    @GetMapping("/filter")
    public ResponseEntity<List<PetInfo>> getByTypeAndStatus(
            @Parameter(description = "Tipo do pet", required = true)
            @RequestParam String petType,
            @Parameter(description = "Status do pet", required = true)
            @RequestParam String status) {
        log.info("Received request to get pet info by type: {} and status: {}", petType, status);
        try {
            List<PetInfo> petInfoList = petInfoRepository.findByPetTypeAndPetStatus(petType, status);
            return ResponseEntity.ok(petInfoList);
        } catch (Exception e) {
            log.error("Error fetching pet info by type and status", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
