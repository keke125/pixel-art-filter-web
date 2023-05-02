package com.keke125.pixel.data.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageFile extends AbstractEntity {

    private Path imagePath;
}