package com.spmss.intelliswine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spmss.intelliswine.entity.Farm;

public interface FarmRepository extends JpaRepository<Farm, Long> {

    List<Farm> findByOwnerId(Long ownerId);
}
