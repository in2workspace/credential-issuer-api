package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record Signer(
        @JsonProperty("commonName") String commonName,
        @JsonProperty("country") String country,
        @JsonProperty("emailAddress") String emailAddress,
        @JsonProperty("organization") String organization,
        @JsonProperty("organizationIdentifier") String organizationIdentifier,
        @JsonProperty("serialNumber") String serialNumber) {
}