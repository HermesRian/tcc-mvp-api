package com.petapp.petapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonBackReference
    private FacebookPost post;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Embedded
    private Media media;

    @Embedded
    private Target target;

    @Column(name = "type")
    private String type;

    @Column(name = "url", length = 1000)
    private String url;
}
