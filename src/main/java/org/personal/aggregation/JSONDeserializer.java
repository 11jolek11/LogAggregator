package org.personal.aggregation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.personal.utils.LogEntry;

import java.io.IOException;

public class JSONDeserializer implements Deserializer{

    private final ObjectMapper objectMapper;

    public JSONDeserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public LogEntry deserialize(String serialized) throws IOException {
        try {
            return this.objectMapper.readValue(serialized, LogEntry.class);
        } catch (JsonProcessingException exception) {
            throw new IOException();
        }
    }
}
