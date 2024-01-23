package com.solutionchallenge.tinypoof.domain.baby.controller;

import com.solutionchallenge.tinypoof.domain.baby.controller.model.req.PostBabyInfoReq;
import com.solutionchallenge.tinypoof.domain.baby.controller.model.res.GetBabyDiaryRes;
import com.solutionchallenge.tinypoof.domain.baby.controller.model.res.GetBabyInfoRes;
import com.solutionchallenge.tinypoof.domain.baby.controller.model.res.PostBabyPoofRes;
import com.solutionchallenge.tinypoof.domain.baby.service.BabyPoofService;
import com.solutionchallenge.tinypoof.domain.baby.service.BabyService;

import com.solutionchallenge.tinypoof.domain.baby.service.GoogleOauthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/baby")
public class BabyController {

    private final BabyPoofService babyPoofService;
    private final BabyService babyService;

    private final GoogleOauthService googleOauthService;

    @PostMapping("/poof")
    public ResponseEntity<PostBabyPoofRes> postBabyPoof( @RequestPart("file") MultipartFile multipartFile, @RequestPart("id") Long id) throws Exception {

        PostBabyPoofRes postBabyPoofRes = babyPoofService.postBabyPoof(multipartFile,id);
        return ResponseEntity.ok(postBabyPoofRes);
    }

    @PostMapping("/info")
    public ResponseEntity<GetBabyInfoRes> postBabyInfo(@RequestBody PostBabyInfoReq postBabyInfoReq){

        GetBabyInfoRes getBabyInfoRes= babyService.postBabyInfo(postBabyInfoReq.getName(),postBabyInfoReq.getBirthDate());

        return ResponseEntity.ok().body(getBabyInfoRes);

    }

    @GetMapping("/info/{babyId}")
    public ResponseEntity<GetBabyInfoRes> getBabyInfo(@PathVariable("babyId")Long babyId){

        GetBabyInfoRes getBabyInfoRes= babyService.getBabyInfo(babyId);

        return ResponseEntity.ok().body(getBabyInfoRes);

    }

    @PostMapping("/diary")
    public ResponseEntity<GetBabyDiaryRes> postBabyDiary(@RequestPart("file") MultipartFile multipartFile, @RequestPart("id") Long id, @RequestPart("story") String story) throws IOException {

        GetBabyDiaryRes getBabyDiaryRes = babyService.postBabyDiary(multipartFile,story);

        return ResponseEntity.ok().body(getBabyDiaryRes);

    }


    @GetMapping("/diary")
    public ResponseEntity<List<GetBabyDiaryRes>> getBabyDiaryAll(){
        List<GetBabyDiaryRes> getBabyDiaryResList = babyService.getBabyDiaryAll();

        return ResponseEntity.ok().body(getBabyDiaryResList);

    }
    @GetMapping("/diary/{diaryId}")
    public ResponseEntity<GetBabyDiaryRes> getBabyDiary(@PathVariable("diaryId")Long diaryId){

        GetBabyDiaryRes getBabyDiaryRes = babyService.getBabyDiary(diaryId);

        return ResponseEntity.ok().body(getBabyDiaryRes);

    }
}
