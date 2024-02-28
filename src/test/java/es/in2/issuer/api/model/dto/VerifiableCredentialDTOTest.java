package es.in2.issuer.api.model.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VerifiableCredentialDTOTest {

    @Test
    public void testConstructorAndGetters() {
        // Arrange
        String expectedFormat = "jwt_vc_json";
        String expectedCredential = "LUpixVCWJk0eOt4CXQe1NXK....WZwmhmn9OQp6YxX0a2L";
        String expectedCNonce = "fGFF7UkhLA";
        int expectedCNonceExpiresIn = 35;

        // Act
        VerifiableCredentialDTO credentialDTO = new VerifiableCredentialDTO(
                expectedFormat,
                expectedCredential,
                expectedCNonce,
                expectedCNonceExpiresIn
        );

        // Assert
        assertEquals(expectedFormat, credentialDTO.getFormat());
        assertEquals(expectedCredential, credentialDTO.getCredential());
        assertEquals(expectedCNonce, credentialDTO.getCNonce());
        assertEquals(expectedCNonceExpiresIn, credentialDTO.getCNonceExpiresIn());
    }

    @Test
    public void lombokGeneratedMethodsTest() {
        // Arrange
        String expectedFormat = "jwt_vc_json";
        String expectedCredential = "LUpixVCWJk0eOt4CXQe1NXK....WZwmhmn9OQp6YxX0a2L";
        String expectedCNonce = "fGFF7UkhLA";
        int expectedCNonceExpiresIn = 35;

        // Act
        VerifiableCredentialDTO dto1 = new VerifiableCredentialDTO(
                expectedFormat,
                expectedCredential,
                expectedCNonce,
                expectedCNonceExpiresIn
        );
        VerifiableCredentialDTO dto2 = new VerifiableCredentialDTO(
                expectedFormat,
                expectedCredential,
                expectedCNonce,
                expectedCNonceExpiresIn
        );

        // Assert
        assertEquals(dto1, dto2); // Tests equals() method generated by Lombok
        assertEquals(dto1.hashCode(), dto2.hashCode()); // Tests hashCode() method generated by Lombok
        assertEquals("VerifiableCredentialDTO(format=" + expectedFormat +
                ", credential=" + expectedCredential +
                ", cNonce=" + expectedCNonce +
                ", cNonceExpiresIn=" + expectedCNonceExpiresIn + ")", dto1.toString()); // Tests toString() method generated by Lombok
    }
}