package com.keke125.pixel.data.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageInfo extends AbstractEntity {
    private String filterType;
    private Integer colorNumber;
    private Integer pixelSize;
    private Integer smooth;
    private Integer edgeCrispening;
    private Integer saturation;
    private Integer contrastRatio;
    private boolean isPublic;
    private File imageOriginalFile;
    private File imageNewFile;
    private String imageOriginalName;
    private String ownerName;
}