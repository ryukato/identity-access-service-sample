package app.domain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;

public class EndUserApplicationJsonSerializer extends JsonSerializer<List<ApplicationEndUser>> {


    @Override
    public void serialize(List<ApplicationEndUser> value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        if (value != null) {
            gen.writeStartArray();
            value.stream().forEach(ae -> {
                try {
                    gen.writeString(ae.getApplication().getId());
                }catch (IOException e) {
                    //do nothing
                }
            });
            gen.writeEndArray();
        }
    }
}
