package es.in2.issuer.application.workflow.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.InvalidOrMissingProofException;
import es.in2.issuer.domain.model.dto.*;
import es.in2.issuer.domain.service.*;
import es.in2.issuer.infrastructure.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static es.in2.issuer.domain.util.Constants.JWT_VC;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifiableCredentialIssuanceServiceImplTest {

    @Mock
    private VerifiableCredentialService verifiableCredentialService;

    @Mock
    private AppConfig appConfig;

    @Mock
    private ProofValidationService proofValidationService;

    @Mock
    private EmailService emailService;

    @Mock
    private CredentialProcedureService credentialProcedureService;

    @Mock
    private DeferredCredentialMetadataService deferredCredentialMetadataService;

    @Mock
    private TrustFrameworkService trustFrameworkService;

    @InjectMocks
    private VerifiableCredentialIssuanceWorkflowImpl verifiableCredentialIssuanceWorkflow;

    @Test
    void completeWithdrawLearCredentialProcessSuccess() throws JsonProcessingException {
        String processId = "1234";
        String type = "LEARCredentialEmployee";
        String json = """
                {
                    "life_span": {
                        "end_date_time": "2025-04-02 09:23:22.637345122 +0000 UTC",
                        "start_date_time": "2024-04-02 09:23:22.637345122 +0000 UTC"
                    },
                    "mandatee": {
                        "email": "example@in2.es",
                        "first_name": "Jhon",
                        "last_name": "Doe",
                        "mobile_phone": "+34666336699"
                    },
                    "mandator": {
                        "commonName": "IN2",
                        "country": "ES",
                        "emailAddress": "rrhh@in2.es",
                        "organization": "IN2, Ingeniería de la Información, S.L.",
                        "organizationIdentifier": "VATES-B60645900",
                        "serialNumber": "B60645900"
                    },
                    "power": [
                        {
                            "id": "6b8f3137-a57a-46a5-97e7-1117a20142fv",
                            "tmf_domain": "DOME",
                            "tmf_function": "DomePlatform",
                            "tmf_type": "Domain",
                            "tmf_action": [
                                "Operator",
                                "Customer",
                                "Provider"
                            ]
                        },
                        {
                            "id": "6b8f3137-a57a-46a5-97e7-1117a20142fb",
                            "tmf_action": "Execute",
                            "tmf_domain": "DOME",
                            "tmf_function": "Onboarding",
                            "tmf_type": "Domain"
                        },
                        {
                            "id": "ad9b1509-60ea-47d4-9878-18b581d8e19b",
                            "tmf_action": [
                                "Create",
                                "Update"
                            ],
                            "tmf_domain": "DOME",
                            "tmf_function": "ProductOffering",
                            "tmf_type": "Domain"
                        }
                    ]
                }
                """;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(json);
        IssuanceRequest issuanceRequest = IssuanceRequest.builder().payload(jsonNode).build();
        String transactionCode = "4321";
        String issuerUIExternalDomain = "https://issuer-ui.com";
        String walletUrl = "https://wallet.com";

        when(verifiableCredentialService.generateVc(processId,type, issuanceRequest)).thenReturn(Mono.just(transactionCode));
        when(appConfig.getIssuerUiExternalDomain()).thenReturn(issuerUIExternalDomain);
        when(appConfig.getWalletUrl()).thenReturn(walletUrl);
        when(emailService.sendTransactionCodeForCredentialOffer("example@in2.es","Credential Offer",issuerUIExternalDomain + "/credential-offer?transaction_code=" + transactionCode, "Jhon",walletUrl)).thenReturn(Mono.empty());

        StepVerifier.create(verifiableCredentialIssuanceWorkflow.completeIssuanceCredentialProcess(processId,type, issuanceRequest))
                .verifyComplete();
    }

    @Test
    void generateVerifiableCredentialResponseSuccess(){
        String processId = "1234";
        CredentialRequest credentialRequest = CredentialRequest.builder()
                .format(JWT_VC)
                .proof(Proof.builder()
                        .proofType("jwt")
                        .jwt("eyJraWQiOiJkaWQ6a2V5OnpEbmFlbjIzd003NmdwaVNMSGt1NGJGRGJzc1ZTOXN0eTl4M0s3eVZxamJTZFRQV0MjekRuYWVuMjN3TTc2Z3BpU0xIa3U0YkZEYnNzVlM5c3R5OXgzSzd5VnFqYlNkVFBXQyIsInR5cCI6Im9wZW5pZDR2Y2ktcHJvb2Yrand0IiwiYWxnIjoiRVMyNTYifQ.eyJpc3MiOiJkaWQ6a2V5OnpEbmFlbjIzd003NmdwaVNMSGt1NGJGRGJzc1ZTOXN0eTl4M0s3eVZxamJTZFRQV0MiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwNzEiLCJleHAiOjE3MTI5MTcwNDAsImlhdCI6MTcxMjA1MzA0MCwibm9uY2UiOiI4OVh4bXdMMlJtR2wyUlp1LU1UU3lRPT0ifQ.DdaaNm4vTn60njLtAQ7Q5oGsQILfA-5h9-sv4MBcVyNBAfSrUUajZqlUukT-5Bx8EqocSvf0RIFRHLcvO9_LMg")
                        .build())
                .build();
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIyQ1ltNzdGdGdRNS1uU2stU3p4T2VYYUVOUTRoSGRkNkR5U2NYZzJFaXJjIn0.eyJleHAiOjE3MTAyNDM2MzIsImlhdCI6MTcxMDI0MzMzMiwiYXV0aF90aW1lIjoxNzEwMjQwMTczLCJqdGkiOiJmY2NhNzU5MS02NzQyLTRjMzAtOTQ5Yy1lZTk3MDcxOTY3NTYiLCJpc3MiOiJodHRwczovL2tleWNsb2FrLXByb3ZpZGVyLmRvbWUuZml3YXJlLmRldi9yZWFsbXMvZG9tZSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJlMmEwNjZmNS00YzAwLTQ5NTYtYjQ0NC03ZWE1ZTE1NmUwNWQiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhY2NvdW50LWNvbnNvbGUiLCJzZXNzaW9uX3N0YXRlIjoiYzFhMTUyYjYtNWJhNy00Y2M4LWFjOTktN2Q2ZTllODIyMjk2IiwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyJdfX0sInNjb3BlIjoib3BlbmlkIGVtYWlsIHByb2ZpbGUiLCJzaWQiOiJjMWExNTJiNi01YmE3LTRjYzgtYWM5OS03ZDZlOWU4MjIyOTYiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJQcm92aWRlciBMZWFyIiwicHJlZmVycmVkX3VzZXJuYW1lIjoicHJvdmlkZXItbGVhciIsImdpdmVuX25hbWUiOiJQcm92aWRlciIsImZhbWlseV9uYW1lIjoiTGVhciJ9.F8vTSNAMc5Fmi-KO0POuaMIxcjdpWxNqfXH3NVdQP18RPKGI5eJr5AGN-yKYncEEzkM5_H28abJc1k_lx7RjnERemqesY5RwoBpTl9_CzdSFnIFbroNOAY4BGgiU-9Md9JsLrENk5Na_uNV_Q85_72tmRpfESqy5dMVoFzWZHj2LwV5dji2n17yf0BjtaWailHdwbnDoSqQab4IgYsExhUkCLCtZ3O418BG9nrSvP-BLQh_EvU3ry4NtnnWxwi5rNk4wzT4j8rxLEAJpMMv-5Ew0z7rbFX3X3UW9WV9YN9eV79-YrmxOksPYahFQwNUXPckCXnM48ZHZ42B0H4iOiA";
        String jti = "fcca7591-6742-4c30-949c-ee9707196756";
        String did = "did:key:zDnaen23wM76gpiSLHku4bFDbssVS9sty9x3K7yVqjbSdTPWC";
        VerifiableCredentialResponse verifiableCredentialResponse = VerifiableCredentialResponse.builder()
                .credential("credential")
                .transactionId("4321")
                .build();
        String procedureId = "123456";
        String mandatorEmail = "jhon@gmail.com";

        when(proofValidationService.isProofValid(credentialRequest.proof().jwt(), token)).thenReturn(Mono.just(true));
        when(verifiableCredentialService.buildCredentialResponse(processId,did,jti,credentialRequest.format(), token, "A")).thenReturn(Mono.just(verifiableCredentialResponse));
        when(deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(jti)).thenReturn(Mono.just(procedureId));
        when(credentialProcedureService.getSignerEmailFromDecodedCredentialByProcedureId(procedureId)).thenReturn(Mono.just(mandatorEmail));
        when(emailService.sendPendingCredentialNotification(mandatorEmail,"Pending Credential")).thenReturn(Mono.empty());
        when(deferredCredentialMetadataService.getOperationModeByAuthServerNonce(jti)).thenReturn(Mono.just("A"));
        when(trustFrameworkService.registerParticipant(did)).thenReturn(Mono.empty());

        StepVerifier.create(verifiableCredentialIssuanceWorkflow.generateVerifiableCredentialResponse(processId,credentialRequest, token))
                .expectNext(verifiableCredentialResponse)
                .verifyComplete();
    }

    @Test
    void generateVerifiableCredentialResponseSyncSuccess(){
        String processId = "1234";
        CredentialRequest credentialRequest = CredentialRequest.builder()
                .format(JWT_VC)
                .proof(Proof.builder()
                        .proofType("jwt")
                        .jwt("eyJraWQiOiJkaWQ6a2V5OnpEbmFlbjIzd003NmdwaVNMSGt1NGJGRGJzc1ZTOXN0eTl4M0s3eVZxamJTZFRQV0MjekRuYWVuMjN3TTc2Z3BpU0xIa3U0YkZEYnNzVlM5c3R5OXgzSzd5VnFqYlNkVFBXQyIsInR5cCI6Im9wZW5pZDR2Y2ktcHJvb2Yrand0IiwiYWxnIjoiRVMyNTYifQ.eyJpc3MiOiJkaWQ6a2V5OnpEbmFlbjIzd003NmdwaVNMSGt1NGJGRGJzc1ZTOXN0eTl4M0s3eVZxamJTZFRQV0MiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwNzEiLCJleHAiOjE3MTI5MTcwNDAsImlhdCI6MTcxMjA1MzA0MCwibm9uY2UiOiI4OVh4bXdMMlJtR2wyUlp1LU1UU3lRPT0ifQ.DdaaNm4vTn60njLtAQ7Q5oGsQILfA-5h9-sv4MBcVyNBAfSrUUajZqlUukT-5Bx8EqocSvf0RIFRHLcvO9_LMg")
                        .build())
                .build();
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIyQ1ltNzdGdGdRNS1uU2stU3p4T2VYYUVOUTRoSGRkNkR5U2NYZzJFaXJjIn0.eyJleHAiOjE3MTAyNDM2MzIsImlhdCI6MTcxMDI0MzMzMiwiYXV0aF90aW1lIjoxNzEwMjQwMTczLCJqdGkiOiJmY2NhNzU5MS02NzQyLTRjMzAtOTQ5Yy1lZTk3MDcxOTY3NTYiLCJpc3MiOiJodHRwczovL2tleWNsb2FrLXByb3ZpZGVyLmRvbWUuZml3YXJlLmRldi9yZWFsbXMvZG9tZSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJlMmEwNjZmNS00YzAwLTQ5NTYtYjQ0NC03ZWE1ZTE1NmUwNWQiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhY2NvdW50LWNvbnNvbGUiLCJzZXNzaW9uX3N0YXRlIjoiYzFhMTUyYjYtNWJhNy00Y2M4LWFjOTktN2Q2ZTllODIyMjk2IiwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyJdfX0sInNjb3BlIjoib3BlbmlkIGVtYWlsIHByb2ZpbGUiLCJzaWQiOiJjMWExNTJiNi01YmE3LTRjYzgtYWM5OS03ZDZlOWU4MjIyOTYiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJQcm92aWRlciBMZWFyIiwicHJlZmVycmVkX3VzZXJuYW1lIjoicHJvdmlkZXItbGVhciIsImdpdmVuX25hbWUiOiJQcm92aWRlciIsImZhbWlseV9uYW1lIjoiTGVhciJ9.F8vTSNAMc5Fmi-KO0POuaMIxcjdpWxNqfXH3NVdQP18RPKGI5eJr5AGN-yKYncEEzkM5_H28abJc1k_lx7RjnERemqesY5RwoBpTl9_CzdSFnIFbroNOAY4BGgiU-9Md9JsLrENk5Na_uNV_Q85_72tmRpfESqy5dMVoFzWZHj2LwV5dji2n17yf0BjtaWailHdwbnDoSqQab4IgYsExhUkCLCtZ3O418BG9nrSvP-BLQh_EvU3ry4NtnnWxwi5rNk4wzT4j8rxLEAJpMMv-5Ew0z7rbFX3X3UW9WV9YN9eV79-YrmxOksPYahFQwNUXPckCXnM48ZHZ42B0H4iOiA";
        String jti = "fcca7591-6742-4c30-949c-ee9707196756";
        String did = "did:key:zDnaen23wM76gpiSLHku4bFDbssVS9sty9x3K7yVqjbSdTPWC";
        VerifiableCredentialResponse verifiableCredentialResponse = VerifiableCredentialResponse.builder()
                .credential("credential")
                .transactionId("4321")
                .build();
        String procedureId = "123456";

        when(proofValidationService.isProofValid(credentialRequest.proof().jwt(), token)).thenReturn(Mono.just(true));
        when(verifiableCredentialService.buildCredentialResponse(processId,did,jti,credentialRequest.format(), token, "S")).thenReturn(Mono.just(verifiableCredentialResponse));
        when(deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(jti)).thenReturn(Mono.just(procedureId));
        when(deferredCredentialMetadataService.getOperationModeByAuthServerNonce(jti)).thenReturn(Mono.just("S"));
        when(deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(jti)).thenReturn(Mono.just("procedureId"));
        when(credentialProcedureService.updateCredentialProcedureCredentialStatusToValidByProcedureId("procedureId")).thenReturn(Mono.empty());
        when(deferredCredentialMetadataService.deleteDeferredCredentialMetadataByAuthServerNonce(jti)).thenReturn(Mono.empty());
        when(trustFrameworkService.registerParticipant(did)).thenReturn(Mono.empty());

        StepVerifier.create(verifiableCredentialIssuanceWorkflow.generateVerifiableCredentialResponse(processId,credentialRequest, token))
                .expectNext(verifiableCredentialResponse)
                .verifyComplete();
    }

    @Test
    void generateVerifiableCredentialResponseFailedProofException(){
        String processId = "1234";
        CredentialRequest credentialRequest = CredentialRequest.builder()
                .format(JWT_VC)
                .proof(Proof.builder()
                        .proofType("jwt")
                        .jwt("eyJraWQiOiJkaWQ6a2V5OnpEbmFlbjIzd003NmdwaVNMSGt1NGJGRGJzc1ZTOXN0eTl4M0s3eVZxamJTZFRQV0MjekRuYWVuMjN3TTc2Z3BpU0xIa3U0YkZEYnNzVlM5c3R5OXgzSzd5VnFqYlNkVFBXQyIsInR5cCI6Im9wZW5pZDR2Y2ktcHJvb2Yrand0IiwiYWxnIjoiRVMyNTYifQ.eyJpc3MiOiJkaWQ6a2V5OnpEbmFlbjIzd003NmdwaVNMSGt1NGJGRGJzc1ZTOXN0eTl4M0s3eVZxamJTZFRQV0MiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwNzEiLCJleHAiOjE3MTI5MTcwNDAsImlhdCI6MTcxMjA1MzA0MCwibm9uY2UiOiI4OVh4bXdMMlJtR2wyUlp1LU1UU3lRPT0ifQ.DdaaNm4vTn60njLtAQ7Q5oGsQILfA-5h9-sv4MBcVyNBAfSrUUajZqlUukT-5Bx8EqocSvf0RIFRHLcvO9_LMg")
                        .build())
                .build();
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIyQ1ltNzdGdGdRNS1uU2stU3p4T2VYYUVOUTRoSGRkNkR5U2NYZzJFaXJjIn0.eyJleHAiOjE3MTAyNDM2MzIsImlhdCI6MTcxMDI0MzMzMiwiYXV0aF90aW1lIjoxNzEwMjQwMTczLCJqdGkiOiJmY2NhNzU5MS02NzQyLTRjMzAtOTQ5Yy1lZTk3MDcxOTY3NTYiLCJpc3MiOiJodHRwczovL2tleWNsb2FrLXByb3ZpZGVyLmRvbWUuZml3YXJlLmRldi9yZWFsbXMvZG9tZSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJlMmEwNjZmNS00YzAwLTQ5NTYtYjQ0NC03ZWE1ZTE1NmUwNWQiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhY2NvdW50LWNvbnNvbGUiLCJzZXNzaW9uX3N0YXRlIjoiYzFhMTUyYjYtNWJhNy00Y2M4LWFjOTktN2Q2ZTllODIyMjk2IiwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyJdfX0sInNjb3BlIjoib3BlbmlkIGVtYWlsIHByb2ZpbGUiLCJzaWQiOiJjMWExNTJiNi01YmE3LTRjYzgtYWM5OS03ZDZlOWU4MjIyOTYiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJQcm92aWRlciBMZWFyIiwicHJlZmVycmVkX3VzZXJuYW1lIjoicHJvdmlkZXItbGVhciIsImdpdmVuX25hbWUiOiJQcm92aWRlciIsImZhbWlseV9uYW1lIjoiTGVhciJ9.F8vTSNAMc5Fmi-KO0POuaMIxcjdpWxNqfXH3NVdQP18RPKGI5eJr5AGN-yKYncEEzkM5_H28abJc1k_lx7RjnERemqesY5RwoBpTl9_CzdSFnIFbroNOAY4BGgiU-9Md9JsLrENk5Na_uNV_Q85_72tmRpfESqy5dMVoFzWZHj2LwV5dji2n17yf0BjtaWailHdwbnDoSqQab4IgYsExhUkCLCtZ3O418BG9nrSvP-BLQh_EvU3ry4NtnnWxwi5rNk4wzT4j8rxLEAJpMMv-5Ew0z7rbFX3X3UW9WV9YN9eV79-YrmxOksPYahFQwNUXPckCXnM48ZHZ42B0H4iOiA";

        when(proofValidationService.isProofValid(credentialRequest.proof().jwt(), token)).thenReturn(Mono.just(false));

        StepVerifier.create(verifiableCredentialIssuanceWorkflow.generateVerifiableCredentialResponse(processId,credentialRequest, token))
                .expectError(InvalidOrMissingProofException.class)
                .verify();
    }

    @Test
    void bindAccessTokenByPreAuthorizedCodeSuccess(){
        String processId = "1234";
        AuthServerNonceRequest authServerNonceRequest = AuthServerNonceRequest.builder()
                .accessToken("ey1234")
                .preAuthorizedCode("4321")
                .build();

        when(verifiableCredentialService.bindAccessTokenByPreAuthorizedCode(processId,authServerNonceRequest.accessToken(),authServerNonceRequest.preAuthorizedCode()))
                .thenReturn(Mono.empty());
        StepVerifier.create(verifiableCredentialIssuanceWorkflow.bindAccessTokenByPreAuthorizedCode(processId,authServerNonceRequest))
                .verifyComplete();
    }

}