package es.in2.issuer.api.model.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializerProvider;
import es.in2.issuer.api.util.Utils;
import id.walt.credentials.w3c.templates.VcTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VcTemplateSerializer extends JsonSerializer<VcTemplate> {

    @Override
    public void serialize(VcTemplate value, JsonGenerator gen, SerializerProvider serializers) {
        try {
            gen.writeRawValue(Utils.toJsonString(value, PropertyNamingStrategies.LOWER_CAMEL_CASE));
        } catch (Exception e) {
            log.error("VcTemplateSerializerException {}", e.getMessage());
        }
    }
}