package com.solutionchallenge.tinypoof.domain.baby.controller.model.res;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@Builder
public class PostBabyPoofRes {

    private Long id;
    private String text;
}
