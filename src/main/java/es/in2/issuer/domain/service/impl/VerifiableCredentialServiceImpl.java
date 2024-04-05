package es.in2.issuer.domain.service.impl;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.util.Base64URL;
import com.upokecenter.cbor.CBORObject;
import es.in2.issuer.domain.exception.*;
import es.in2.issuer.domain.model.*;
import es.in2.issuer.domain.service.AuthenticSourcesRemoteService;
import es.in2.issuer.domain.service.RemoteSignatureService;
import es.in2.issuer.domain.service.VerifiableCredentialService;
import es.in2.issuer.domain.util.Utils;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import es.in2.issuer.infrastructure.repository.CacheStore;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import nl.minvws.encoding.Base45;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static es.in2.issuer.domain.util.Constants.CREDENTIAL_SUBJECT;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerifiableCredentialServiceImpl implements VerifiableCredentialService {
//
//    // fixme: this is a temporary solution to load credential templates from resources
//    @Value("classpath:credentials/templates/LEARCredentialTemplate.json")
//    private Resource learCredentialTemplate;
//
//    private final RemoteSignatureService remoteSignatureService;
//    private final AuthenticSourcesRemoteService authenticSourcesRemoteService;
//    private final CacheStore<String> cacheStore;
//    private final AppConfiguration appConfiguration;
//
//    private String issuerDid;
//    @PostConstruct
//    private void initializeAzureProperties() {
//        issuerDid = appConfiguration.getIssuerDid();
//    }
//
//
//    @Override
//    public Mono<VerifiableCredentialResponse> generateVerifiableCredentialResponse(
//            String username,
//            CredentialRequest credentialRequest,
//            String token
//    ) {
//        return getNonceClaim(credentialRequest.proof().jwt())
//                //TODO: revisar si el nonce está en el cache, Eliminar el nonce del cache después de la comprobación. Si el nonce no está en el cache es que ya fue usado: lanzar una exception.
//                .flatMap(nonceClaim -> Mono.fromRunnable(() -> cacheStore.delete(nonceClaim))
//                        .thenReturn(nonceClaim))
//                .flatMap(nonceClaim -> extractDidFromJwtProof(credentialRequest.proof().jwt())
//                        .flatMap(subjectDid -> {
//                            String format = credentialRequest.format();
//                            return generateVerifiableCredential(username, token, subjectDid, format)
//                                    .map(credential -> new VerifiableCredentialResponse(format, credential, nonceClaim, 600));
//                        }));
//    }
//
//    private Mono<String> generateVerifiableCredential(String username, String token, String subjectDid, String format) {
//        return Mono.defer(() -> {
//            try {
//                Instant expiration = Instant.now().plus(30, ChronoUnit.DAYS);
//                // Prepare desired custom data that should replace the default template data
//                log.info("Fetching information from authentic sources ...");
//                //return authenticSourcesRemoteService.getUser(token)
//                return authenticSourcesRemoteService.getUserFromLocalFile()
//                        .flatMap(appUser -> {
//                            log.info("Getting credential subject data for credentialType: " + es.in2.issuer.domain.util.Constants.LEAR_CREDENTIAL + " ...");
//                            Map<String, Map<String, String>> credentialSubject = appUser.credentialSubjectData();
//                            Map<String, String> credentialSubjectData;
//                            if (!credentialSubject.isEmpty()) {
//                                credentialSubjectData = credentialSubject.getOrDefault(es.in2.issuer.domain.util.Constants.LEAR_CREDENTIAL, null);
//                            } else {
//                                return Mono.error(new NoSuchElementException("No data saved for CredentialType " + es.in2.issuer.domain.util.Constants.LEAR_CREDENTIAL + " and username " + username + "."));
//                            }
//                            Map<String, Object> data = Map.of(CREDENTIAL_SUBJECT, credentialSubjectData);
//
//                            // todo get issuer did from dss module
//                            // Get Credential template from local file
//                            String learTemplate;
//                            try {
//                                learTemplate = new String(learCredentialTemplate.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
//                            } catch (IOException e) {
//                                throw new RuntimeException(e);
//                            }
//
//                            // Create VC according to the format and sign it
//                            return Mono.just(learTemplate)
//                                    .flatMap(template -> generateVcPayLoad(template, subjectDid, issuerDid, data, expiration))
//                                    .flatMap(vcString -> {
//                                        if(format.equals("jwt_vc")){
//                                            log.info(vcString);
//                                            log.info("Signing credential in JADES remotely ...");
//                                            SignatureRequest signatureRequest = new SignatureRequest(
//                                                    new SignatureConfiguration(SignatureType.JADES, Collections.emptyMap()),
//                                                    vcString
//                                            );
//                                            return remoteSignatureService.sign(signatureRequest, token)
//                                                    .publishOn(Schedulers.boundedElastic())
//                                                    // save credential in emitted credential list
//                                                    //.doOnSuccess(signedData -> commitCredentialSourceData(vcPayload, token).subscribe())
//                                                    .map(SignedData::data);
//                                        } else if (format.equals("cwt_vc")) {
//                                            log.info(vcString);
//                                            return generateCborFromJson(vcString)
//                                                    .flatMap(cbor -> generateCOSEBytesFromCBOR(cbor, token))
//                                                    .flatMap(this::compressAndConvertToBase45FromCOSE);
//                                        } else {
//                                            return Mono.error(new IllegalArgumentException("Unsupported credential format: " + format));
//                                        }
//                                    });
//                        });
//            } catch (UserDoesNotExistException e) {
//                log.error("UserDoesNotExistException {}", e.getMessage());
//                return Mono.error(new RuntimeException(e));
//            }
//        });
//    }
//
//    /**
//     * Generate CBOR payload for COSE.
//     *
//     * @param edgcJson EDGC payload as JSON string
//     * @return Mono emitting CBOR bytes
//     */
//    private Mono<byte[]> generateCborFromJson(String edgcJson) {
//        return Mono.fromCallable(() -> {
//            CBORObject cborObject = CBORObject.FromJSONString(edgcJson);
//            return cborObject.EncodeToBytes();
//        });
//    }
//
//    /**
//     * Generate COSE bytes from CBOR bytes.
//     *
//     * @param cbor  CBOR bytes
//     * @param token Authentication token
//     * @return Mono emitting COSE bytes
//     */
//    private Mono<byte[]> generateCOSEBytesFromCBOR(byte[] cbor, String token) {
//        log.info("Signing credential in COSE format remotely ...");
//        String cborBase64 = Base64.getEncoder().encodeToString(cbor);
//        SignatureRequest signatureRequest = new SignatureRequest(
//                new SignatureConfiguration(SignatureType.COSE, Collections.emptyMap()),
//                cborBase64
//        );
//        return remoteSignatureService.sign(signatureRequest, token).map(signedData -> Base64.getDecoder().decode(signedData.data()));
//    }
//
//    /**
//     * Compress COSE bytes and convert it to Base45.
//     *
//     * @param cose COSE Bytes
//     * @return Mono emitting COSE bytes compressed and in Base45
//     */
//    private Mono<String> compressAndConvertToBase45FromCOSE(byte[] cose) {
//        return Mono.fromCallable(() -> {
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            try (CompressorOutputStream deflateOut = new CompressorStreamFactory()
//                    .createCompressorOutputStream(CompressorStreamFactory.DEFLATE, stream)) {
//                deflateOut.write(cose);
//            } // Automatically closed by try-with-resources
//            byte[] zip = stream.toByteArray();
//            return Base45.getEncoder().encodeToString(zip);
//        }).onErrorResume(e -> {
//            log.error("Error compressing and converting to Base45: " + e.getMessage(), e);
//            return Mono.error(new Base45Exception("Error compressing and converting to Base45"));
//        });
//    }
//
//    public Mono<Void> commitCredentialSourceData(Payload vcPayload, String token) {
//        log.info("Committing credential source data ...");
//        return Mono.defer(() -> {
//            Map<String, Object> vcJSON = vcPayload.toJSONObject();
//            Map<String, Object> vcInfo = (Map<String, Object>) vcJSON.get("vc");
//            Map<String, Object> userInfo = (Map<String, Object>) vcInfo.get(CREDENTIAL_SUBJECT);
//            IDEPCommitCredential idepCommitCredential;
//            try {
//                idepCommitCredential = new IDEPCommitCredential(
//                        UUID.fromString(((String) vcInfo.get("id")).split(":")[1]),
//                        (String) userInfo.get("serialNumber"),
//                        Utils.createDate((String) vcInfo.get("expirationDate"))
//                );
//            } catch (ParseException e) {
//                log.error("Error creating Date {}", e.getMessage());
//                return Mono.error(new CreateDateException("Error creating Date"));
//            }
//            return authenticSourcesRemoteService.commitCredentialSourceData(idepCommitCredential, token)
//                    .onErrorResume(e -> {
//                        // Handle the exception with a custom exception
//                        log.error("Error committing credential source data: " + e.getMessage());
//                        return Mono.empty();
//                    });
//        });
//    }

    @Override
    public Mono<String> generateVcPayLoad(String vcTemplate, String subjectDid, String issuerDid, Map<String, Object> userData, Instant expiration) throws JSONException {
        return Mono.fromCallable(()->{
            // Parse vcTemplate to a JSON object
            JSONObject vcTemplateObject = new JSONObject(vcTemplate);

            // Generate a unique UUID for jti and vc.id
            String uuid = "urn:uuid:" + UUID.randomUUID().toString();

            // Calculate timestamps
            Instant nowInstant = Instant.now();
            long nowTimestamp = nowInstant.getEpochSecond();
            long expTimestamp = expiration.getEpochSecond();


            // Update vcTemplateObject with dynamic values
            vcTemplateObject.put("id", uuid);
            vcTemplateObject.put("issuer", new JSONObject().put("id", issuerDid));
            // Update issuanceDate, issued, validFrom, expirationDate in vcTemplateObject using ISO 8601 format
            String nowDateStr = nowInstant.toString();
            String expirationDateStr = expiration.toString();
            vcTemplateObject.put("issuanceDate", nowDateStr);
            vcTemplateObject.put("issued", nowDateStr);
            vcTemplateObject.put("validFrom", nowDateStr);
            vcTemplateObject.put("expirationDate", expirationDateStr);

            // Convert userData map contents to Object and set as credentialSubject
            Object credentialSubjectValue = userData.get("credentialSubject");
            vcTemplateObject.put("credentialSubject", new JSONObject((Map) credentialSubjectValue));

            // Construct final JSON Object
            JSONObject finalObject = new JSONObject();
            finalObject.put("sub", subjectDid);
            finalObject.put("nbf", nowTimestamp);
            finalObject.put("iss", issuerDid);
            finalObject.put("exp", expTimestamp);
            finalObject.put("iat", nowTimestamp);
            finalObject.put("jti", uuid);
            finalObject.put("vc", vcTemplateObject);

            // Return final object as String
            return finalObject.toString();
        });
    }
//
//    private Mono<String> getNonceClaim(String jwtProof) {
//        return Mono.fromCallable(() -> {
//            JWSObject jwsObject = JWSObject.parse(jwtProof);
//            return jwsObject.getPayload().toJSONObject().get("nonce").toString();
//        });
//    }
//
//    private Mono<String> extractDidFromJwtProof(String jwtProof) {
//        return Mono.fromCallable(() -> {
//            JWSObject jwsObject = JWSObject.parse(jwtProof);
//            // Extract the issuer DID from the kid claim in the header
//            return jwsObject.getHeader().toJSONObject().get("kid").toString();
//        });
//    }

}