package com.petapp.petapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacebookImageDTO {
    private Integer height;
    private String src;
    private Integer width;
}
