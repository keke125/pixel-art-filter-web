package com.keke125.pixel.data.service;

import com.keke125.pixel.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    User findByUsername(String username);

    @Query("select u from User u " +
            "where u.username like  :username")
    List<User> findAllByUsername(@Param("username") String username);

    @Query("select u from User u " +
            "where u.email like  :email")
    List<User> findAllByEmail(@Param("email") String email);
}
