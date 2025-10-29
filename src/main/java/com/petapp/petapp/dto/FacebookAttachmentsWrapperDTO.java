package com.petapp.petapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacebookAttachmentsWrapperDTO {
    private List<FacebookAttachmentDTO> data;
}
