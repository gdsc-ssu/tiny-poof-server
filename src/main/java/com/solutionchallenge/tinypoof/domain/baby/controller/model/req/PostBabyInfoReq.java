package com.solutionchallenge.tinypoof.domain.baby.controller.model.req;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostBabyInfoReq {

    private String name;
    private String birthDate;
}
