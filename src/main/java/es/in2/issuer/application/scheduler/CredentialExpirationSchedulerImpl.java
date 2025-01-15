package es.in2.issuer.application.scheduler;

import es.in2.issuer.domain.model.entities.CredentialProcedure;
import es.in2.issuer.domain.model.enums.CredentialStatus;
import es.in2.issuer.infrastructure.repository.CredentialProcedureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.Timestamp;
import java.time.Instant;

@Service
@Slf4j
@EnableScheduling
public class CredentialExpirationSchedulerImpl implements CredentialExpirationScheduler {

    private final CredentialProcedureRepository credentialProcedureRepository;

    public CredentialExpirationSchedulerImpl(CredentialProcedureRepository credentialProcedureRepository) {
        this.credentialProcedureRepository = credentialProcedureRepository;
    }

    @Override
    @Scheduled(cron = "0 0 1 * * ?") //Cada día a la 1:00 AM
    public void checkAndExpireCredentials() {
        log.info("Scheduled Task - Ejecutando checkAndExpireCredentials a: {}", Instant.now());

        credentialProcedureRepository.findAll()
                .filter(this::isExpired)
                .flatMap(this::expireCredential)
                .publishOn(Schedulers.boundedElastic())
                .subscribe();
    }

    private boolean isExpired(CredentialProcedure credentialProcedure) {
        if (credentialProcedure.getValidUntil() == null) {
            return false;
        }

        Instant validUntil = credentialProcedure.getValidUntil().toInstant();
        return validUntil.isBefore(Instant.now());
    }

    Mono<CredentialProcedure> expireCredential(CredentialProcedure credentialProcedure) {
        if(credentialProcedure.getCredentialStatus() != CredentialStatus.EXPIRED) {
            credentialProcedure.setCredentialStatus(CredentialStatus.EXPIRED);
            credentialProcedure.setUpdatedAt(Timestamp.from(Instant.now()));
            log.info("Expirando credencial con ID: {} - Nuevo estado: {}",
                    credentialProcedure.getCredentialId(),
                    credentialProcedure.getCredentialStatus());
            return credentialProcedureRepository.save(credentialProcedure);
        }
        return Mono.empty();
    }
}