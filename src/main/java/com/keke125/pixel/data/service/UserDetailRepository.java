package com.keke125.pixel.data.service;

import com.keke125.pixel.data.entity.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserDetailRepository extends JpaRepository<UserDetail, Long>, JpaSpecificationExecutor<UserDetail> {

}
