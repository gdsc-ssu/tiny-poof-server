package com.solutionchallenge.tinypoof.domain.baby.controller.model.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetS3ImageRes {
    private String imageUrl;
    private Long userId;
}
