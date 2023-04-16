package com.keke125.pixel.data.service;

import com.keke125.pixel.data.entity.SampleImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SampleImageRepository
        extends
        JpaRepository<SampleImage, Long>,
        JpaSpecificationExecutor<SampleImage> {

}