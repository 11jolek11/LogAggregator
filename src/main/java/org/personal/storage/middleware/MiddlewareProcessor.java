package org.personal.storage.middleware;

import org.personal.utils.LogEntry;

public interface MiddlewareProcessor {
    default boolean canProcess(LogEntry logEntry) {
        return true;
    };
    LogEntry process(LogEntry logEntry);
}
