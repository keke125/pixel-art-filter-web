package com.keke125.pixel.data.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageInfo extends AbstractEntity {

    private Integer colorNumber;
    private Integer pixelSize;
    private String smooth;
    private String edgeCrispening;
    private Integer saturation;
    private Integer contrastRatio;
    private boolean isPublic;
    private ImageFile imageFile;
}