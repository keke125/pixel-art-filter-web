package com.keke125.pixel.data.service;

import com.keke125.pixel.data.entity.ImageInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageInfoRepository
        extends
        JpaRepository<ImageInfo, Long>,
        JpaSpecificationExecutor<ImageInfo> {

    @Query("select i from ImageInfo i " +
            "where i.ownerName like  :ownerName")
    List<ImageInfo> findAllByOwnerName(@Param("ownerName") String ownerName);

    @Query("select count(i)from ImageInfo i " +
            "where i.ownerName like  :ownerName")
    long countByOwnerName(@Param("ownerName") String ownerName);
}