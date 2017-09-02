package app.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class NullableDeSerializer<T> extends JsonDeserializer<T> {
    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return null;
    }
}