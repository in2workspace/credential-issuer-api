package es.in2.issuer.application.scheduler;

import reactor.core.publisher.Mono;

public interface CredentialExpirationScheduler {
    void checkAndExpireCredentials();
}