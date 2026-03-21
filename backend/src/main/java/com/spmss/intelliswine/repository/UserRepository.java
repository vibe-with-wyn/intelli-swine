package com.spmss.intelliswine.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spmss.intelliswine.entity.Users;

public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmailAddress(String emailAddress);

    boolean existsByEmailAddress(String emailAddress);

    boolean existsByUsername(String username);
}
