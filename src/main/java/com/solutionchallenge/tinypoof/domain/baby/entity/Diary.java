package com.solutionchallenge.tinypoof.domain.baby.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.solutionchallenge.tinypoof.global.utils.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
