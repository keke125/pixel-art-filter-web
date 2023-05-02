package com.keke125.pixel.data.service;

import com.keke125.pixel.data.entity.SampleImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SampleImageService {

    private final SampleImageRepository repository;

    public SampleImageService(SampleImageRepository repository) {
        this.repository = repository;
    }

    public Optional<SampleImage> get(Long id) {
        return repository.findById(id);
    }

    public SampleImage update(SampleImage entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<SampleImage> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<SampleImage> list(Pageable pageable, Specification<SampleImage> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public void save(SampleImage entity) {
        repository.save(entity);
    }

}