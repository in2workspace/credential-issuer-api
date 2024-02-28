package es.in2.issuer.api.model.dto;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CredentialOfferForPreAuthorizedCodeFlowTest {

    @Test
    public void testConstructorAndGetters() {
        // Arrange
        String expectedCredentialIssuer = "https://credential-issuer.example.com";
        List<String> expectedCredentials = List.of("UniversityDegree");
        Map<String, Grant> expectedGrants = new HashMap<>();
        expectedGrants.put("grant1", new Grant("type1", false));
        expectedGrants.put("grant2", new Grant("type2", false));

        // Act
        CredentialOfferForPreAuthorizedCodeFlow credentialOffer = new CredentialOfferForPreAuthorizedCodeFlow(
                expectedCredentialIssuer,
                expectedCredentials,
                expectedGrants
        );

        // Assert
        assertEquals(expectedCredentialIssuer, credentialOffer.getCredentialIssuer());
        assertEquals(expectedCredentials, credentialOffer.getCredentials());
        assertEquals(expectedGrants, credentialOffer.getGrants());
    }

    @Test
    public void testSetters() {
        // Arrange
        CredentialOfferForPreAuthorizedCodeFlow offer = new CredentialOfferForPreAuthorizedCodeFlow();
        String newCredentialIssuer = "https://new-credential-issuer.example.com";
        List<String> newCredentials = List.of("NewDegree");
        Map<String, Grant> newGrants = new HashMap<>();

        // Act
        offer.setCredentialIssuer(newCredentialIssuer);
        offer.setCredentials(newCredentials);
        offer.setGrants(newGrants);

        // Assert
        assertEquals(newCredentialIssuer, offer.getCredentialIssuer());
        assertEquals(newCredentials, offer.getCredentials());
        assertEquals(newGrants, offer.getGrants());
    }

    @Test
    public void lombokGeneratedMethodsTest() {
        // Arrange
        String expectedCredentialIssuer = "https://credential-issuer.example.com";
        List<String> expectedCredentials = List.of("UniversityDegree");
        Map<String, Grant> expectedGrants = new HashMap<>();
        expectedGrants.put("grant1", new Grant("type1", false));
        expectedGrants.put("grant2", new Grant("type2", false));

        // Act
        CredentialOfferForPreAuthorizedCodeFlow offer1 = new CredentialOfferForPreAuthorizedCodeFlow(
                expectedCredentialIssuer,
                expectedCredentials,
                expectedGrants
        );
        CredentialOfferForPreAuthorizedCodeFlow offer2 = new CredentialOfferForPreAuthorizedCodeFlow(
                expectedCredentialIssuer,
                expectedCredentials,
                expectedGrants
        );

        // Assert
        assertEquals(offer1, offer2); // Tests equals() method generated by Lombok
        assertEquals(offer1.hashCode(), offer2.hashCode()); // Tests hashCode() method generated by Lombok
        assertEquals("CredentialOfferForPreAuthorizedCodeFlow(credentialIssuer=" + expectedCredentialIssuer +
                ", credentials=" + expectedCredentials +
                ", grants=" + expectedGrants + ")", offer1.toString()); // Tests toString() method generated by Lombok
    }
}