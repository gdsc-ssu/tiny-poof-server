package com.solutionchallenge.tinypoof.domain.baby.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.solutionchallenge.tinypoof.domain.baby.controller.model.res.PostBabyPoofRes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class BabyPoofService {

    @Value("${gemini.project-id}")
    private String projectId;

    @Value("${gemini.region}")
    private String region;

    @Value("${gemini.model-name}")
    private String modelName;
    private final GoogleOauthService googleOauthService;

   public PostBabyPoofRes postBabyPoof(MultipartFile multipartFile,Long id) throws Exception {

        // 구글 oauth먼저 진행
        String accessToken = googleOauthService.getAccessToken();

        // 이미지 base64 인코딩
        String imageEncoding = encodeMultipartFileToBase64(multipartFile);

        // 프롬프트 메세징과 보낼 데이터 json으로 직렬화
        String encodingImage = imageEncoding;

        // prompt 메시지
        String promptMessage = "what is Baby stool color Tell me? and Tell me how baby is doing for that color simply.";

       // Jackson ObjectMapper 생성
       ObjectMapper objectMapper = new ObjectMapper();

       // contents 객체 생성
       ObjectNode contentsObject = objectMapper.createObjectNode();
       contentsObject.put("role", "USER");

       // parts 배열 생성
       ArrayNode partsArray = objectMapper.createArrayNode();

       // inlineData 객체 생성
       ObjectNode inlineDataObject = objectMapper.createObjectNode();
       inlineDataObject.put("mimeType", "image/png");
       inlineDataObject.put("data", encodingImage);

       // parts 배열에 inlineData 추가
       ObjectNode inlineDataWrapper = objectMapper.createObjectNode();
       inlineDataWrapper.set("inlineData", inlineDataObject);
       partsArray.add(inlineDataWrapper);

       // text 객체 생성
       ObjectNode textObject = objectMapper.createObjectNode();
       textObject.put("text", promptMessage);

       // parts 배열에 text 추가
       partsArray.add(textObject);

       // USER 객체에 parts 배열 추가
       contentsObject.set("parts", partsArray);

       // safetySettings 배열 생성
       ArrayNode safetySettingsArray = objectMapper.createArrayNode();

       // safetySettings 객체 생성
       ObjectNode safetySettingsObject = objectMapper.createObjectNode();
       safetySettingsObject.put("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT");
       safetySettingsObject.put("threshold", "BLOCK_NONE");

       // safetySettings 배열에 객체 추가
       safetySettingsArray.add(safetySettingsObject);

       // generationConfig 객체 생성
       ObjectNode generationConfigObject = objectMapper.createObjectNode();
       generationConfigObject.put("temperature", 0.4);
       generationConfigObject.put("topP", 1.0);
       generationConfigObject.put("topK", 32);
       generationConfigObject.put("maxOutputTokens", 100);

       // 전체 JSON 데이터 생성
       ObjectNode jsonNodes = objectMapper.createObjectNode();
       jsonNodes.set("contents", contentsObject);
       jsonNodes.set("safetySettings", safetySettingsArray);
       jsonNodes.set("generationConfig", generationConfigObject);


        String geminiResponse = googleOauthService.geminiResponse(accessToken,jsonNodes);


        // 구글 쪽에 API 쏘기

        return PostBabyPoofRes.builder()
                .text(geminiResponse)
                .id(id).build();
    }

    public String encodeMultipartFileToBase64(MultipartFile multipartFile) throws IOException {
        byte[] fileContent = multipartFile.getBytes();
        byte[] encodedBytes = Base64.getEncoder().encode(fileContent);
        return new String(encodedBytes);
    }
}
