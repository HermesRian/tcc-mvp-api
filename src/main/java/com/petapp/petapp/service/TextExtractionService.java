package com.petapp.petapp.service;

import com.petapp.petapp.entity.PetInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TextExtractionService {

    // Padrões de regex para extração
    private static final Pattern PET_TYPE_PATTERN = Pattern.compile(
        "\\b(gato|gata|cachorro|cachorra|cão|cadela|pássaro|pássara|ave|coelho|coelha|hamster)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern STATUS_PATTERN = Pattern.compile(
        "\\b(desaparecid[oa]|perdid[oa]|sumid[oa]|encontrad[oa]|achad[oa]|resgatad[oa])\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern LOCATION_PATTERN = Pattern.compile(
        "(?:na região d[oa]|no bairro d[oa]|em|próximo [aà]|perto d[oa]|na rua|na avenida)\\s+([^.,;]+)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DATE_PATTERN = Pattern.compile(
        "\\b(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{2,4}))?\\b"
    );

    private static final Pattern TIME_PERIOD_PATTERN = Pattern.compile(
        "\\b(manhã|tarde|noite|madrugada|meio-dia|meia-noite)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern COLOR_PATTERN = Pattern.compile(
        "\\b(preto|preta|branco|branca|marrom|cinza|amarelo|amarela|laranja|tigrado|tigrada|malhado|malhada|caramelo)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SIZE_PATTERN = Pattern.compile(
        "\\b(pequeno|pequena|médio|média|grande|filhote|filhota|adulto|adulta|idoso|idosa)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CONTACT_PATTERN = Pattern.compile(
        "\\b(?:contato|telefone|whatsapp|tel|fone|ligar)[:\\s]*([\\d\\s()-]+)",
        Pattern.CASE_INSENSITIVE
    );

    public PetInfo extractInformation(String description) {
        if (description == null || description.trim().isEmpty()) {
            log.warn("Empty description provided for extraction");
            return null;
        }

        log.info("Extracting information from description: {}", description);

        PetInfo petInfo = new PetInfo();
        petInfo.setOriginalDescription(description);

        int extractedFields = 0;

        // Extrair tipo de pet
        String petType = extractPetType(description);
        if (petType != null) {
            petInfo.setPetType(petType);
            extractedFields++;
            log.debug("Extracted pet type: {}", petType);
        }

        // Extrair status
        String status = extractStatus(description);
        if (status != null) {
            petInfo.setPetStatus(status);
            extractedFields++;
            log.debug("Extracted status: {}", status);
        }

        // Extrair localização
        String location = extractLocation(description);
        if (location != null) {
            petInfo.setLocation(location.trim());
            extractedFields++;
            log.debug("Extracted location: {}", location);
        }

        // Extrair data
        LocalDate eventDate = extractDate(description);
        if (eventDate != null) {
            petInfo.setEventDate(eventDate);
            extractedFields++;
            log.debug("Extracted date: {}", eventDate);
        }

        // Extrair período do dia
        String timePeriod = extractTimePeriod(description);
        if (timePeriod != null) {
            petInfo.setEventTimePeriod(timePeriod);
            extractedFields++;
            log.debug("Extracted time period: {}", timePeriod);
        }

        // Extrair cor
        String color = extractColor(description);
        if (color != null) {
            petInfo.setColor(color);
            extractedFields++;
            log.debug("Extracted color: {}", color);
        }

        // Extrair tamanho
        String size = extractSize(description);
        if (size != null) {
            petInfo.setSize(size);
            extractedFields++;
            log.debug("Extracted size: {}", size);
        }

        // Extrair contato
        String contact = extractContact(description);
        if (contact != null) {
            petInfo.setContactInfo(contact.trim());
            extractedFields++;
            log.debug("Extracted contact: {}", contact);
        }

        // Calcular confiança da extração (0.0 a 1.0)
        double confidence = calculateConfidence(extractedFields);
        petInfo.setExtractionConfidence(confidence);

        log.info("Extraction completed. Fields extracted: {}, Confidence: {}", extractedFields, confidence);

        return petInfo;
    }

    private String extractPetType(String text) {
        Matcher matcher = PET_TYPE_PATTERN.matcher(text);
        if (matcher.find()) {
            String type = matcher.group(1).toLowerCase();
            // Normalizar variações
            if (type.matches("gato|gata")) return "Gato";
            if (type.matches("cachorro|cachorra|cão|cadela")) return "Cachorro";
            if (type.matches("pássaro|pássara|ave")) return "Pássaro";
            if (type.matches("coelho|coelha")) return "Coelho";
            if (type.matches("hamster")) return "Hamster";
        }
        return null;
    }

    private String extractStatus(String text) {
        Matcher matcher = STATUS_PATTERN.matcher(text);
        if (matcher.find()) {
            String status = matcher.group(1).toLowerCase();
            // Normalizar variações
            if (status.matches("desaparecid[oa]|perdid[oa]|sumid[oa]")) return "Desaparecido";
            if (status.matches("encontrad[oa]|achad[oa]|resgatad[oa]")) return "Encontrado";
        }
        return null;
    }

    private String extractLocation(String text) {
        Matcher matcher = LOCATION_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private LocalDate extractDate(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                int day = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                String yearStr = matcher.group(3);

                int year;
                if (yearStr != null) {
                    year = Integer.parseInt(yearStr);
                    if (year < 100) {
                        year += 2000; // Assume 20xx para anos de 2 dígitos
                    }
                } else {
                    // Se o ano não foi fornecido, usar o ano atual
                    year = LocalDate.now().getYear();
                }

                return LocalDate.of(year, month, day);
            } catch (Exception e) {
                log.warn("Failed to parse date from: {}", matcher.group(0), e);
            }
        }
        return null;
    }

    private String extractTimePeriod(String text) {
        Matcher matcher = TIME_PERIOD_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).toLowerCase();
        }
        return null;
    }

    private String extractColor(String text) {
        Matcher matcher = COLOR_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).toLowerCase();
        }
        return null;
    }

    private String extractSize(String text) {
        Matcher matcher = SIZE_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).toLowerCase();
        }
        return null;
    }

    private String extractContact(String text) {
        Matcher matcher = CONTACT_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private double calculateConfidence(int extractedFields) {
        // Considerando 8 campos principais
        int totalFields = 8;
        double confidence = (double) extractedFields / totalFields;

        // Ajustar confiança (mínimo de 0.1 se algo foi extraído)
        if (extractedFields > 0 && confidence < 0.1) {
            confidence = 0.1;
        }

        return Math.round(confidence * 100.0) / 100.0;
    }
}
