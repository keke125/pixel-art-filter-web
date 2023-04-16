package com.keke125.pixel.data.service;

import com.keke125.pixel.data.entity.UserDetail;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class UserDetailService {

    private final UserDetailRepository repository;

    public UserDetailService(UserDetailRepository repository) {
        this.repository = repository;
    }

    public Optional<UserDetail> get(Long id) {
        return repository.findById(id);
    }

    public UserDetail update(UserDetail entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<UserDetail> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<UserDetail> list(Pageable pageable, Specification<UserDetail> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
