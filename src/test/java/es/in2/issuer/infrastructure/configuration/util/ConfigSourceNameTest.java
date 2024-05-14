package es.in2.issuer.infrastructure.configuration.util;

import es.in2.issuer.infrastructure.configuration.model.ConfigProviderName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigSourceNameTest {

    @Test
    void testConditionMatches() {
        // Mock Environment
        Environment environment = mock(Environment.class);
        when(environment.getProperty("app.config-source.name")).thenReturn("yaml");

        // Mock ConditionContext
        ConditionContext context = mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(environment);

        // Mock AnnotationMetadata
        AnnotationMetadata metadata = mock(AnnotationMetadata.class);
        when(metadata.getAnnotationAttributes(ConfigSourceName.class.getName()))
                .thenReturn(java.util.Collections.singletonMap("name", "yaml"));


        // Create instance of ConfigSourceNameCondition
        ConfigSourceNameCondition condition = new ConfigSourceNameCondition();

        // Mock the behavior to return YAML as the expected ConfigProviderName
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", ConfigProviderName.YAML);
        when(metadata.getAnnotationAttributes(ConfigSourceName.class.getName()))
                .thenReturn(attributes);

        // Evaluate the condition
        boolean matches = condition.matches(context, metadata);

        // Assertion
        Assertions.assertTrue(matches, "Condition should match for YAML config provider");
    }
}
