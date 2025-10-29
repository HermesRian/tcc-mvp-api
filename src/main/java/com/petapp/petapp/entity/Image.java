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
public class Image {

    @Column(name = "image_height")
    private Integer height;

    @Column(name = "image_src", length = 1000)
    private String src;

    @Column(name = "image_width")
    private Integer width;
}
