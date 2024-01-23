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


    private String privateKeyStr = "-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCXSQLWPWrXt/dV\n0+J7M6G/fYl4odRYeBGvVCu7OUrxypNN5yCCBYcoDM/vPzeB9M4C+UbrV+2U/Ih5\npiacGoMsJpZgKhr6M8ESEXtNcxFyn/NbcxauhgArUsdtXhLt86TrVNl5RWA7LJjM\nZZvvtJ+FF82aJW5N+l1yZl8QbB9V7DTnkd+uSS5+qaKPzAHV0ktw6qamuhiAr81n\ndmfxSXJ7bZOh+8Ba8h87iR9VLyeVmg0ftkyE+Pasdn5E8GWjj+iZLYSk6GX+8czC\ncljaZYqfMiHiI7FeU66UVzusxG2s0qJQU0FVFkPlRboVEVA1FGnPgJJh0LLIWDYF\nwEhX9Cb7AgMBAAECggEAFt2krZ/70Z4PJqxPW4K6zFgbmp2PHE4Nkjv9Hh+zOad+\nKzSnX6+ZNY72MLmT2LMZHi7yLqkMEkw3Dnfz9v003Uv5x5AlorKG2IXG5rZhKTuO\ny0Ayae98ABBTby7cWpXMU/tvoVpAiMEdWSt0bb8Vq0alP0qwCN4yGJPrg25sMA63\nlK5M0qIKkjOnku1RE2Q161CynpTmIkLutPt1rlptzk3qlVI1rR9N2h3U4DCfpe4/\n1mhsXwCHWbGLnjX0Iz3tyCww5U/yz2+lFUEy6N389inx7LGgeOL2F1mBBLS0CtoK\nx1kS3VuZbOhHRgx21SRNmf0SPvDG0WBZEMv8JoQKQQKBgQDFyjnAt2xR6vtEC2/l\nCInDdzysEZ+h7/fiz1OOeQ28acdsdHUH/S+UnKxYHUdCiLMcfnz1vBPrGBDyV79Z\nmGaws5LXkrgKwNU0OBf1t212AOpH6CQ+MSzQDr1ZmUg3SvINNvLk4UTVyDVZiJZe\nKGFKaIOrjO4C3J9Bwhzf3I4XwQKBgQDDzw1dzmg4qp/+IP3Yei/NmVeb7wPUuX+4\nC5CoSv12FeUI8mZm7CmBbAxEZPPM2ItaoDWVmd+sZw4/6Xsoxv7Bcz6DhsAZTdbN\nk00CNMP1CF+m1xS6dFQcMOuHR6iyZPn1RLH1c7c0LvtPNjTBcmFbsHiz0JT0OR8k\ny8IwofQNuwKBgGcxsFolGQXQdNI2qWW0w7PQ9T3NYB0sR4f1peGytQD1q4+hXyIQ\n7g86gwziEbjb+59FO0s2mqnQ/56BO5uyUPmmZ1nD+fPaUiKRlaak0h58Mb/clXRB\nUeEnn5Es0SnQF2PfpHDYO4LSl0skYVe097XLu874k4G4u/qiHVCLETPBAoGBAJu4\nEwIx/zJphxlb9WRuL4wLUeABogNPjmG4WM9t68o4OU/3LjbVw2BYpBzUSAr1f0WQ\nOW0C1R3PS0bKkJZqQvlnhCSnSigVfik45rpFaj147lDJpYNuteFBil2oQGG26g4D\nFp+D6gOHBXcGPchGBWUcn7EoxPzAXKMvo/x3H5a3AoGBAJkARdEInvY4rtG4dW1u\nS+XkCr1N38YD4Br0ceZzGInjgw0cj0OCRoWVf843tcc12hNWs7tLa2XRP1g6Rm5K\nwoBr/4ukocU8wIsusiUsT34doEB+ISAwhma6fQJoms29f59rJB2KwAo1qWbi87wt\nlJZ0rR0KsZE3uz8WDSNjf6lI\n-----END PRIVATE KEY-----\n";


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
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr.getBytes(StandardCharsets.UTF_8));
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
