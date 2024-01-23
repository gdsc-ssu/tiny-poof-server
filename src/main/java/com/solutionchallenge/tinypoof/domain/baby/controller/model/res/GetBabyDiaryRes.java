package com.solutionchallenge.tinypoof.domain.baby.controller.model.res;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetBabyDiaryRes {
    private Long id;
    private String story;
    private String imageUrl;
}
