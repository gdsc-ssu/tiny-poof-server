package com.solutionchallenge.tinypoof.domain.baby.repository;

import com.solutionchallenge.tinypoof.domain.baby.entity.Baby;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BabyRepository extends JpaRepository<Baby,Long> {

    Optional<Baby> findById(Long id);
}
