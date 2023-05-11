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
    private String filterType;
    private Integer colorNumber;
    private Integer pixelSize;
    private Integer smooth;
    private Integer edgeCrispening;
    private Integer saturation;
    private Integer contrastRatio;
    private boolean isPublic;
    private String imageOriginalFile;
    private String imageNewFile;
    private String imageOriginalName;
    private String imageNewName;
    private String ownerName;
}