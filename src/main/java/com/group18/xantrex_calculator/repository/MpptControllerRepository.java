package com.group18.xantrex_calculator.repository;

import com.group18.xantrex_calculator.entity.MpptController;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MpptControllerRepository extends JpaRepository<MpptController, Long> {
    Optional<MpptController> findByNameIgnoreCase(String name);
}
