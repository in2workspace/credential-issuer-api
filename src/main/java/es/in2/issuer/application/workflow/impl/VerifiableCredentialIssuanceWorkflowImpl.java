package es.in2.issuer.application.workflow.impl;

import com.nimbusds.jose.JWSObject;
import com.upokecenter.cbor.CBORObject;
import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
import es.in2.issuer.application.workflow.DeferredCredentialWorkflow;
import es.in2.issuer.application.workflow.VerifiableCredentialIssuanceWorkflow;
import es.in2.issuer.domain.exception.Base45Exception;
import es.in2.issuer.domain.exception.CredentialTypeUnsupportedException;
import es.in2.issuer.domain.exception.InvalidOrMissingProofException;
import es.in2.issuer.domain.model.dto.*;
import es.in2.issuer.domain.model.enums.SignatureType;
import es.in2.issuer.domain.service.*;
import es.in2.issuer.domain.util.Constants;
import es.in2.issuer.infrastructure.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.minvws.encoding.Base45;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static es.in2.issuer.domain.util.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifiableCredentialIssuanceWorkflowImpl implements VerifiableCredentialIssuanceWorkflow {

    private final VerifiableCredentialService verifiableCredentialService;
    private final AppConfig appConfig;
    private final ProofValidationService proofValidationService;
    private final EmailService emailService;
    private final CredentialProcedureService credentialProcedureService;
    private final DeferredCredentialMetadataService deferredCredentialMetadataService;
    private final CredentialSignerWorkflow credentialSignerWorkflow;
    private final MarketplaceService marketplaceService;

//    @Override
//    public Mono<Void> completeWithdrawCredentialProcess(String processId, String type, CredentialData credentialData, String token) {
//        if (LEAR_CREDENTIAL_EMPLOYEE.equals(type)) {
//            return verifiableCredentialService.generateVc(processId, type, credentialData)
//                    .flatMap(transactionCode -> {
//                        String email = credentialData.payload().get("mandatee").get("email").asText();
//                        String name = credentialData.payload().get("mandatee").get("first_name").asText();
//                        return emailService.sendTransactionCodeForCredentialOffer(email, "Credential Offer", appConfig.getIssuerUiExternalDomain() + "/credential-offer?transaction_code=" + transactionCode, name, appConfig.getWalletUrl());
//                    });
//        } else if (VERIFIABLE_CERTIFICATION.equals(type)) {
//            return verifiableCredentialService.generateVc(processId, type, credentialData)
//                    .flatMap(transactionCode ->
//                            deferredCredentialMetadataService.getProcedureIdByTransactionCode(transactionCode)
//                                    .flatMap(procedureId -> credentialSignerWorkflow.signAndUpdateCredential(token, procedureId))
//                                    .flatMap(signedCredential -> {
//                                        String email = credentialData.payload().get("credentialSubject").get("company").get("email").asText();
//                                        String name = credentialData.payload().get("credentialSubject").get("company").get("commonName").asText();
//                                        return emailService.sendTransactionCodeForCredentialOffer(email, "Credential Offer", appConfig.getIssuerUiExternalDomain() + "/credential-offer?transaction_code=" + transactionCode, name, appConfig.getWalletUrl())
//                                                .thenReturn(signedCredential);
//                                    })
//                                    .flatMap(marketplaceService::sendVerifiableCertificationToMarketplace
//                                    )
//                    );
//        } else {
//            return Mono.error(new CredentialTypeUnsupportedException(type));
//        }
//    }

    @Override
    public Mono<Void> completeWithdrawCredentialProcess(String processId, String type, CredentialData credentialData, String token) {
        if (credentialData.operationMode()==null || credentialData.operationMode().equals(SYNC)) {
            return verifiableCredentialService.generateVc(processId, type, credentialData)
                    .flatMap(transactionCode -> sendCredentialOfferEmail(type, transactionCode, credentialData));
        } else if (credentialData.operationMode().equals(ASYNC)) {
            return verifiableCredentialService.generateVc(processId, type, credentialData)
                    .flatMap(transactionCode ->
                            deferredCredentialMetadataService.getProcedureIdByTransactionCode(transactionCode)
                                    .flatMap(procedureId -> credentialSignerWorkflow.signAndUpdateCredential(token, procedureId))
                                    //TODO: envio de credential offer al email para emision asíncrona
//                                    .flatMap(signedCredential -> sendCredentialOfferEmail(type, transactionCode, credentialData)
//                                            .thenReturn(signedCredential))
                                    .flatMap(marketplaceService::sendVerifiableCertificationToMarketplace
                                    )
                    );
        } else {
            return Mono.error(new CredentialTypeUnsupportedException(type));
        }
    }

    private Mono<Void> sendCredentialOfferEmail(String type, String transactionCode, CredentialData credentialData){
        String email = credentialData.payload().get("mandatee").get("email").asText();
        String name = credentialData.payload().get("mandatee").get("first_name").asText();
        return emailService.sendTransactionCodeForCredentialOffer(email, "Credential Offer", appConfig.getIssuerUiExternalDomain() + "/credential-offer?transaction_code=" + transactionCode, name, appConfig.getWalletUrl());
        //TODO: envio de credential offer al email condicional al tipo de VC
//        if (LEAR_CREDENTIAL_EMPLOYEE.equals(type)) {
//            String email = credentialData.credential().get("mandatee").get("email").asText();
//            String name = credentialData.credential().get("mandatee").get("first_name").asText();
//            return emailService.sendTransactionCodeForCredentialOffer(email, "Credential Offer", appConfig.getIssuerUiExternalDomain() + "/credential-offer?transaction_code=" + transactionCode, name, appConfig.getWalletUrl());
//        } else if (VERIFIABLE_CERTIFICATION.equals(type)) {
//            String email = credentialData.payload().get("credentialSubject").get("company").get("email").asText();
//            String name = credentialData.payload().get("credentialSubject").get("company").get("commonName").asText();
//            return emailService.sendTransactionCodeForCredentialOffer(email, "Credential Offer", appConfig.getIssuerUiExternalDomain() + "/credential-offer?transaction_code=" + transactionCode, name, appConfig.getWalletUrl());
//        } else {
//            return Mono.error(new CredentialTypeUnsupportedException(type));
//        }
    }


//    @Override
//    public Mono<VerifiableCredentialResponse> generateVerifiableCredentialResponse(
//            String processId,
//            CredentialRequest credentialRequest,
//            String token
//    ) {
//        try {
//            JWSObject jwsObject = JWSObject.parse(token);
//            String authServerNonce = jwsObject.getPayload().toJSONObject().get("jti").toString();
//
//            if (VERIFIABLE_CERTIFICATION.equals(credentialRequest.credentialIdentifier())) {
//                // Skip proof validation and return null for subjectDid
//                return verifiableCredentialService.buildCredentialResponse(processId, null, authServerNonce, credentialRequest.format());
//            } else if (LEAR_CREDENTIAL_EMPLOYEE.equals(credentialRequest.credentialIdentifier())) {
//                return proofValidationService.isProofValid(credentialRequest.proof().jwt(), token)
//                        .flatMap(isValid -> {
//                            if (Boolean.FALSE.equals(isValid)) {
//                                return Mono.error(new InvalidOrMissingProofException("Invalid proof"));
//                            } else {
//                                return extractDidFromJwtProof(credentialRequest.proof().jwt());
//                            }
//                        })
//                        .flatMap(subjectDid -> verifiableCredentialService.buildCredentialResponse(processId, subjectDid, authServerNonce, credentialRequest.format())
//                                .flatMap(credentialResponse -> deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(authServerNonce)
//                                        .flatMap(credentialProcedureService::getSignerEmailFromDecodedCredentialByProcedureId)
//                                        .flatMap(email -> emailService.sendPendingCredentialNotification(email, "Pending Credential")
//                                                .then(Mono.just(credentialResponse)))));
//            } else {
//                return Mono.error(new IllegalArgumentException("Unsupported credential Identifier"));
//            }
//        } catch (ParseException e) {
//            log.error("Error parsing the accessToken", e);
//            throw new RuntimeException("Error parsing accessToken", e);
//        }
//    }

    @Override
    public Mono<VerifiableCredentialResponse> generateVerifiableCredentialResponse(
            String processId,
            CredentialRequest credentialRequest,
            String token
    ) {
        try {
            JWSObject jwsObject = JWSObject.parse(token);
            String authServerNonce = jwsObject.getPayload().toJSONObject().get("jti").toString();
            return proofValidationService.isProofValid(credentialRequest.proof().jwt(), token)
                    .flatMap(isValid -> {
                        if (Boolean.FALSE.equals(isValid)) {
                            return Mono.error(new InvalidOrMissingProofException("Invalid proof"));
                        } else {
                            return extractDidFromJwtProof(credentialRequest.proof().jwt());
                        }
                    })
                    .flatMap(subjectDid -> verifiableCredentialService.buildCredentialResponse(processId, subjectDid, authServerNonce, credentialRequest.format())
                            .flatMap(credentialResponse -> deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(authServerNonce)
                                    .flatMap(credentialProcedureService::getSignerEmailFromDecodedCredentialByProcedureId)
                                    .flatMap(email -> emailService.sendPendingCredentialNotification(email,"Pending Credential")
                                            .then(Mono.just(credentialResponse)))));
        }
        catch (ParseException e){
            log.error("Error parsing the accessToken", e);
            throw new RuntimeException("Error parsing accessToken", e);
        }
    }

    @Override
    public Mono<Void> bindAccessTokenByPreAuthorizedCode(String processId, AuthServerNonceRequest authServerNonceRequest) {
        return verifiableCredentialService.bindAccessTokenByPreAuthorizedCode
                (processId, authServerNonceRequest.accessToken(), authServerNonceRequest.preAuthorizedCode());
    }

    @Override
    public Mono<BatchCredentialResponse> generateVerifiableCredentialBatchResponse(
            String username,
            BatchCredentialRequest batchCredentialRequest,
            String token
    ) {
        return Flux.fromIterable(batchCredentialRequest.credentialRequests())
                .flatMap(credentialRequest -> generateVerifiableCredentialResponse(username, credentialRequest, token)
                        .map(verifiableCredentialResponse -> new BatchCredentialResponse.CredentialResponse(verifiableCredentialResponse.credential())))
                .collectList()
                .map(BatchCredentialResponse::new);
    }

    @Override
    public Mono<VerifiableCredentialResponse> generateVerifiableCredentialDeferredResponse(String processId, DeferredCredentialRequest deferredCredentialRequest) {
                return verifiableCredentialService.generateDeferredCredentialResponse(processId,deferredCredentialRequest)
                .onErrorResume(e -> Mono.error(new RuntimeException("Failed to process the credential for the next processId: " + processId, e)));
    }


    private Mono<String> extractDidFromJwtProof(String jwtProof) {
        return Mono.fromCallable(() -> {
            JWSObject jwsObject = JWSObject.parse(jwtProof);
            // Extract the issuer DID from the kid claim in the header
            String kid = jwsObject.getHeader().toJSONObject().get("kid").toString();
            // Split the kid string at '#' and take the first part
            return kid.split("#")[0];
        });
    }

}
