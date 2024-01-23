package com.solutionchallenge.tinypoof.domain.baby.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.util.SecurityUtils;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import static com.google.api.client.util.SecurityUtils.sign;

@Service
@RequiredArgsConstructor
public class GoogleOauthService {

    private static final RestTemplate restTemplate;

    @Value("${gemini.iss}")
    private String iss;

    @Value("${gemini.region}")
    private String region;

    @Value("${gemini.project-id}")
    private String projectId;

    // Google API Console에서 가져온 비공개 키 (PKCS8 형식)
    @Value("${gemini.model-name}")
    private String model;

    @Value("${gemini.private-key}")
    private String privateKeyStr;

    static {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(50000);

        restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    public String getAccessToken() throws Exception {
        Gson gson = new Gson();
        long currentTimeInSeconds = Instant.now().getEpochSecond();

        // JWT 헤더 생성
        JsonObject header = new JsonObject();
        header.addProperty("alg", "RS256");
        header.addProperty("typ", "JWT");
        String encodedHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(gson.toJson(header).getBytes(StandardCharsets.UTF_8));

        // JWT 클레임 세트 생성
        JsonObject claimSet = new JsonObject();
        claimSet.addProperty("iss", iss);
        claimSet.addProperty("aud", "https://oauth2.googleapis.com/token");
        claimSet.addProperty("scope", "https://www.googleapis.com/auth/cloud-platform");
        claimSet.addProperty("exp", currentTimeInSeconds + 3600);
        claimSet.addProperty("iat", currentTimeInSeconds);
        String encodedClaimSet = Base64.getUrlEncoder().encodeToString(gson.toJson(claimSet).getBytes(StandardCharsets.UTF_8));

        // JWT 서명 생성
        String signatureInput = encodedHeader + "." + encodedClaimSet;
        String signature = sign(signatureInput, privateKeyStr);

        // JWT 생성
        String jwt = signatureInput + "." + signature;


        // OAuth 2.0 토큰 엔드포인트 호출
        ObjectNode jsonNodes = JsonNodeFactory.instance.objectNode();
        //String encodedString = URLEncoder.encode("urn:ietf:params:oauth:grant-type:jwt-bearer", StandardCharsets.UTF_8.toString());
        jsonNodes.put("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        jsonNodes.put("assertion", jwt);

        ResponseEntity<JsonNode> postResult = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token",
                jsonNodes,
                JsonNode.class
        );

        JsonNode responseJson = postResult.getBody();
        if (responseJson != null) {
            return responseJson.get("access_token").asText();
        } else {
            // Handle the case where the response is null (e.g., error handling)
            return null;
        }

    }

    private static String sign(String signatureInput, String privateKeyStr) throws Exception {

        privateKeyStr = privateKeyStr
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(signatureInput.getBytes());
        byte[] signatureBytes = signature.sign();

        return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
    }


    private static JsonElement convertStringToJsonElement(String jsonString) {
        return JsonParser.parseString(jsonString);
    }

    public String geminiResponse(String accessToken,ObjectNode jsonData)  {

        // JSON 데이터 생성
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonNodes = objectMapper.createObjectNode();
        jsonNodes.setAll(jsonData);

// HttpHeaders 생성
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken); // Access Token 설정
        headers.setContentType(MediaType.APPLICATION_JSON);

// HTTP 요청 엔터티 생성
        HttpEntity<JsonNode> httpEntity = new HttpEntity<>(jsonNodes, headers);

        // HTTP POST 요청
        ResponseEntity<JsonNode> postResult = restTemplate.postForEntity(
                "https://"+region+"-aiplatform.googleapis.com/v1/projects/"+projectId+"/locations/"+region+"/publishers/google/models/"+model+":streamGenerateContent",
               httpEntity,
                JsonNode.class
        );
        JsonNode responseJson = postResult.getBody();
        String textValue = responseJson.get(0) // 첫 번째 객체
                .get("candidates").get(0) // 첫 번째 candidates 배열
                .get("content").get("parts").get(0) // parts 배열의 첫 번째 객체
                .get("text").asText();

        return textValue;

    }

}
