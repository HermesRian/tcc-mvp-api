package com.petapp.petapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacebookPostDTO {
    private String id;
    private FacebookAttachmentsWrapperDTO attachments;
}
