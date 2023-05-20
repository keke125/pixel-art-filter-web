package com.keke125.pixel.data.service;

import com.keke125.pixel.data.entity.ImageInfo;
import com.keke125.pixel.data.entity.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repository;
    private final ImageService imageService;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, ImageService imageService, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.imageService = imageService;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> get(Long id) {
        return repository.findById(id);
    }

    public User update(User entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        Optional<User> user = repository.findById(id);
        if (user.isPresent()) {
            List<ImageInfo> imageInfoList = imageService.findAllImageInfosByOwnerName(user.get().getUsername());
            if (!imageInfoList.isEmpty()) {
                try {
                    for (ImageInfo imageInfo : imageInfoList) {
                        imageService.deleteImageInfo(imageInfo);
                    }
                } catch (RuntimeException e) {
                    System.err.printf("Can't delete user's (ID: %d) images!", user.get().getId());
                }
            }
            repository.deleteById(id);
        }
    }

    public Page<User> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<User> list(Pageable pageable, Specification<User> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public void store(User user) {
        repository.save(user);
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public boolean isUsernameNonExist(String username) {
        return repository.findByUsername(username) == null;
    }

    public boolean isEmailExist(String email) {
        return !repository.findAllByEmail(email).isEmpty();
    }

}
