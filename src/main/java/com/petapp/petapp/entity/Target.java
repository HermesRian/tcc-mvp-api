package com.petapp.petapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Target {

    @Column(name = "target_id")
    private String id;

    @Column(name = "target_url", length = 1000)
    private String url;
}
