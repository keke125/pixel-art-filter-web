package com.keke125.pixel.data.service;

import com.keke125.pixel.data.entity.ImageInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ImageInfoService {

    private final ImageInfoRepository repository;

    public ImageInfoService(ImageInfoRepository repository) {
        this.repository = repository;
    }

    public Optional<ImageInfo> get(Long id) {
        return repository.findById(id);
    }

    public ImageInfo update(ImageInfo entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<ImageInfo> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<ImageInfo> list(Pageable pageable, Specification<ImageInfo> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public ImageInfoRepository getRepository() {
        return this.repository;
    }
}