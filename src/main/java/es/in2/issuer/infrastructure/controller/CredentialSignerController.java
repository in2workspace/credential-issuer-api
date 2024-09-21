package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
import es.in2.issuer.domain.model.dto.ProcedureIdRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static es.in2.issuer.domain.util.Constants.JWT_VC;

@Slf4j
@RestController
@RequestMapping("/api/v1/sign-credential")
@RequiredArgsConstructor
public class CredentialSignerController {

    private final CredentialSignerWorkflow credentialSignerWorkflow;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> createVerifiableCredential(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody ProcedureIdRequest procedureIdRequest) {
        return credentialSignerWorkflow.signCredential(authorizationHeader, procedureIdRequest.procedureId(), JWT_VC).then();
    }
}
