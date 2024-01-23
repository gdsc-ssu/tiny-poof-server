package com.solutionchallenge.tinypoof.domain.baby.controller.model.res;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetBabyInfoRes {
    private Long id;
    private String name;
    private String birthDate;
}
