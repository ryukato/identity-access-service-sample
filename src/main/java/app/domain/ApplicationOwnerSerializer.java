package app.domain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Optional;

public class ApplicationOwnerSerializer extends JsonSerializer<Tenant> {
    @Override
    public void serialize(Tenant value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        Optional.ofNullable(value).ifPresent(tenant -> {
            try {
                gen.writeString(tenant.getId());
            }catch (IOException e){
                //do nothing
            }
        });
    }
}
