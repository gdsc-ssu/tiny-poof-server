package com.solutionchallenge.tinypoof.domain.baby.service;

import com.solutionchallenge.tinypoof.domain.baby.controller.model.res.GetBabyDiaryRes;
import com.solutionchallenge.tinypoof.domain.baby.controller.model.res.GetBabyInfoRes;
import com.solutionchallenge.tinypoof.domain.baby.controller.model.res.GetS3ImageRes;
import com.solutionchallenge.tinypoof.domain.baby.entity.Baby;
import com.solutionchallenge.tinypoof.domain.baby.entity.Diary;
import com.solutionchallenge.tinypoof.domain.baby.repository.BabyRepository;
import com.solutionchallenge.tinypoof.domain.baby.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BabyService {

    private final BabyRepository babyRepository;
    private final DiaryRepository diaryRepository;
    private final ImageService imageService;

    public GetBabyInfoRes postBabyInfo(String name, String birthDate){

        Baby baby = Baby.builder()
                .name(name)
                .birthDate(birthDate)
                .build();
        Baby storeBaby = babyRepository.save(baby);
        return GetBabyInfoRes.builder()
                .id(storeBaby.getId())
                .name(storeBaby.getName())
                .birthDate(storeBaby.getBirthDate())
                .build();
    }

    public GetBabyInfoRes getBabyInfo(Long id){

        Optional<Baby> baby = babyRepository.findById(id);
        return GetBabyInfoRes.builder()
                .id(baby.orElseThrow().getId())
                .name(baby.orElseThrow().getName())
                .birthDate(baby.orElseThrow().getBirthDate())
                .build();
    }
    public GetBabyDiaryRes postBabyDiary(MultipartFile multipartFile,String story) throws IOException {

        GetS3ImageRes getS3ImageRes = imageService.upload(multipartFile);
        Diary diary = Diary.builder()
                .story(story)
                .imageUrl(getS3ImageRes.getImageUrl())
                .build();
        Diary storeDiary = diaryRepository.save(diary);

        return GetBabyDiaryRes.builder()
                .id(storeDiary.getId())
                .story(storeDiary.getStory())
                .imageUrl(storeDiary.getImageUrl())
                .build();

    }

    public List<GetBabyDiaryRes> getBabyDiaryAll(){

        List<Diary> diaryList = diaryRepository.findAll();

        List<GetBabyDiaryRes> getBabyDiaryResList = diaryList.stream().map(diary -> GetBabyDiaryRes.builder()
                .imageUrl(diary.getImageUrl())
                .story(diary.getStory())
                .id(diary.getId()).build()).collect(Collectors.toList());
        return getBabyDiaryResList;

    }

    public GetBabyDiaryRes getBabyDiary(Long id){

        Optional<Diary> diary = diaryRepository.findById(id);

        return GetBabyDiaryRes.builder()
                .story(diary.orElseThrow().getStory())
                .imageUrl(diary.orElseThrow().getImageUrl())
                .id(diary.orElseThrow().getId()).build();

    }




}
