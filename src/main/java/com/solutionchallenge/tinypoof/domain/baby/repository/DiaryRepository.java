package com.solutionchallenge.tinypoof.domain.baby.repository;

import com.solutionchallenge.tinypoof.domain.baby.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary,Long> {

    Optional<Diary> findById(Long id);
}
