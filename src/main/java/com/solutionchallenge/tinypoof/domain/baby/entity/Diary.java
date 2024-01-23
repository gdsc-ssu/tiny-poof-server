package com.solutionchallenge.tinypoof.domain.baby.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.solutionchallenge.tinypoof.global.utils.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
public class Diary extends BaseEntity {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String story;
    private String imageUrl;

    public Diary() {

    }
}
