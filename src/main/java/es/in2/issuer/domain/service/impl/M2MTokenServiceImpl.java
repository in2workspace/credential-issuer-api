package es.in2.issuer.domain.service.impl;

import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.model.dto.VerifierOauth2AccessToken;
import es.in2.issuer.domain.service.JWTService;
import es.in2.issuer.domain.service.M2MTokenService;
import es.in2.issuer.infrastructure.config.properties.VerifierProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

import static es.in2.issuer.domain.util.Constants.*;
import static es.in2.issuer.domain.util.Constants.CLIENT_ASSERTION_TYPE_VALUE;

@Service
@Slf4j
@RequiredArgsConstructor
public class M2MTokenServiceImpl implements M2MTokenService {

    private final WebClient oauth2VerifierWebClient;
    private final VerifierProperties verifierProperties;
    private final JWTService jwtService;

    @Override
    public Mono<VerifierOauth2AccessToken> getM2MToken() {
        return oauth2VerifierWebClient.post()
                .uri(verifierProperties.paths().tokenPath())
                .header(CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED_FORM)
                .bodyValue(getM2MFormUrlEncodeBodyValue())
                .retrieve()
                .bodyToMono(VerifierOauth2AccessToken.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("Error fetching token", e)));
    }

    private String getM2MFormUrlEncodeBodyValue() {
        Map<String, String> parameters = Map.of(
                OAuth2ParameterNames.GRANT_TYPE, CLIENT_CREDENTIALS_GRANT_TYPE_VALUE,
                OAuth2ParameterNames.CLIENT_ID, verifierProperties.verifierKey(),
                OAuth2ParameterNames.CLIENT_ASSERTION_TYPE, CLIENT_ASSERTION_TYPE_VALUE,
                OAuth2ParameterNames.CLIENT_ASSERTION, createClientAssertion()
        );

        return parameters.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

    private String createClientAssertion() {

        String vcMachineString = getVCinJWTDecodedFromBase64();
        SignedJWT vcMachineJWT = jwtService.parseJWT(vcMachineString);
        Payload vcMachinePayload = jwtService.getPayloadFromSignedJWT(vcMachineJWT);
        String clientId = jwtService.getClaimFromPayload(vcMachinePayload,"sub");

        String vpTokenJWTString = createVPTokenJWT(vcMachineString,clientId);

        Payload payload = new Payload(Map.of(
                "sub", clientId,
                "iss", clientId,
                "aud", "https://verifier.dome-marketplace-lcl.org",
                "iat", 1725951134,
                "exp", 1760079134,
                "jti", UUID.randomUUID(),
                "vp_token", vpTokenJWTString
        ));

        return jwtService.generateJWT(payload.toString());
    }

    private String createVPTokenJWT(String vcMachineString, String clientId) {
        Map<String, Object> vp = createVP(vcMachineString,clientId);

        Payload payload = new Payload(Map.of(
                "sub", clientId,
                "iss", clientId,
                "nbf", "",
                "iat", 1725951134,
                "exp", 1760079134,
                "jti", UUID.randomUUID(),
                "vp", vp
        ));

        return jwtService.generateJWT(payload.toString());

    }

    private Map<String, Object> createVP(String vcMachineString, String clientId){
        return Map.of(
                "@context", List.of("https://www.w3.org/2018/credentials/v1"),
                "holder", clientId,
                "id", "urn:uuid:" + UUID.randomUUID(),
                "type", List.of("VerifiablePresentation"),
                "verifiableCredential", List.of(vcMachineString)
        );
    }

    private String getVCinJWTDecodedFromBase64(){
        String vcTokenBase64 = verifierProperties.vc();
        byte [] vcTokenDecoded = Base64.getDecoder().decode(vcTokenBase64);
        return new String(vcTokenDecoded);
    }

    @Override
    public Mono<Void> verifyM2MToken(VerifierOauth2AccessToken token) {
        return null;
    }
}
