package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.DeferredCredentialRequest;
import es.in2.issuer.domain.model.LEARCredentialRequest;
import es.in2.issuer.domain.model.VerifiableCredentialResponse;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface VerifiableCredentialService {
    Mono<String> generateVc(String processId, String vcType, LEARCredentialRequest learCredentialRequest);
//    Mono<String> generateVcPayLoad(String vcTemplate, String subjectDid, String issuerDid, String userData, Instant expiration);
    Mono<String> generateDeferredCredentialResponse(String processId, DeferredCredentialRequest deferredCredentialRequest);
    Mono<VerifiableCredentialResponse> buildCredentialResponse(String processId, String subjectDid, String accessToken, String format);

    Mono<Void> bindAccessTokenByPreAuthorizedCode(String processId, String accessToken, String preAuthCode);
}
