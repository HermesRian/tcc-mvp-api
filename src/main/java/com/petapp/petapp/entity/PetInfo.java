package com.petapp.petapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pet_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "facebook_post_id", nullable = false)
    @JsonBackReference
    private FacebookPost facebookPost;

    // Texto original completo
    @Column(name = "original_description", columnDefinition = "TEXT")
    private String originalDescription;

    // Dados extraídos
    @Column(name = "pet_type")
    private String petType; // Ex: "Gato", "Cachorro", "Pássaro"

    @Column(name = "pet_status")
    private String petStatus; // Ex: "desaparecido", "encontrado"

    @Column(name = "location")
    private String location; // Ex: "região do Centro", "Bairro X"

    @Column(name = "event_date")
    private LocalDate eventDate; // Data do evento (desaparecimento/encontro)

    @Column(name = "event_time_period")
    private String eventTimePeriod; // Ex: "tarde", "manhã", "noite"

    // Características físicas
    @Column(name = "color")
    private String color;

    @Column(name = "breed")
    private String breed;

    @Column(name = "size")
    private String size;

    // Informações de contato
    @Column(name = "contact_info")
    private String contactInfo;

    // Confiança da extração (0.0 a 1.0)
    @Column(name = "extraction_confidence")
    private Double extractionConfidence;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
