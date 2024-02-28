package es.in2.issuer.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignatureRequest {

    private final SignatureConfiguration configuration;
    private final String data;
}