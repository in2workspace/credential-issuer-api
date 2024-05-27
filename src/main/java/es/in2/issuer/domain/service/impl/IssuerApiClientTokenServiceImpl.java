package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.PreAuthorizationCodeGetException;
import es.in2.issuer.domain.service.IssuerApiClientTokenService;
import es.in2.issuer.infrastructure.config.AuthServerConfig;
import es.in2.issuer.infrastructure.config.WebClientConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static es.in2.issuer.domain.util.Constants.CONTENT_TYPE;
import static es.in2.issuer.domain.util.Constants.CONTENT_TYPE_URL_ENCODED_FORM;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssuerApiClientTokenServiceImpl implements IssuerApiClientTokenService {
    private final AuthServerConfig authServerConfig;
    private final WebClientConfig webClient;
    private final ObjectMapper objectMapper;
    @Override
    public Mono<String> getClientToken() {

        String clientSecret = authServerConfig.getAuthServerClientSecret().trim();
        String decodedSecret;

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(clientSecret);
            decodedSecret = new String(decodedBytes, StandardCharsets.UTF_8);

            String reEncodedSecret = Base64.getEncoder().encodeToString(decodedSecret.getBytes(StandardCharsets.UTF_8)).trim();
            if (!clientSecret.equals(reEncodedSecret)) {
                decodedSecret = clientSecret;
            }
        } catch (IllegalArgumentException ex) {
            decodedSecret = clientSecret;
        }
        String body = "grant_type=" + URLEncoder.encode("client_credentials", StandardCharsets.UTF_8) +
                "&client_id=" + URLEncoder.encode(authServerConfig.getAuthServerClientId(), StandardCharsets.UTF_8) +
                "&client_secret=" + URLEncoder.encode(decodedSecret, StandardCharsets.UTF_8);

        return webClient.commonWebClient()
                .post()
                .uri(authServerConfig.getTokenUri())
                .header(CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED_FORM)
                .bodyValue(body)
                .exchangeToMono(response -> {
                    if (response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {
                        return Mono.error(new PreAuthorizationCodeGetException("There was an error during token retrieval, error: " + response));
                    } else {
                        log.info("Token response: {}", response);
                        return response.bodyToMono(String.class);
                    }
                }).flatMap(response -> {
                    log.debug(response);
                    Map<String, Object> jsonObject;
                    try {
                        jsonObject = objectMapper.readValue(response, new TypeReference<>() {});
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException(e));
                    }
                    String token = jsonObject.get("access_token").toString();
                    return Mono.just(token);
                });
    }
}
