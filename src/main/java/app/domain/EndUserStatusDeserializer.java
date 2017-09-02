package app.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class EndUserStatusDeserializer extends JsonDeserializer<EndUserStatus> {
    @Override
    public EndUserStatus deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        return EndUserStatus.fromString(jsonParser.getValueAsString());
    }
}
