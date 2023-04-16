package com.keke125.pixel.data.entity;

import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class SampleImage extends AbstractEntity {

    private Integer colorDepth;
    private Integer pixelSize;
    private String smooth;
    private String edgeCrispening;
    private Integer saturation;
    private Integer contrastRatioBox;
    private boolean isPublic;
}