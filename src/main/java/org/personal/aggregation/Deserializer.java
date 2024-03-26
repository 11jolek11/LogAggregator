package org.personal.aggregation;

import org.personal.utils.LogEntry;

import java.io.IOException;

public interface Deserializer {
    public LogEntry deserialize(String serialized) throws IOException;
}
