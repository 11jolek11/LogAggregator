package org.personal.storage.middleware;

import org.personal.utils.LogEntry;

public final class EmptyProcessor implements MiddlewareProcessor{
    private EmptyProcessor() {
    }

    public static EmptyProcessor createEmptyProcessor() {
        return new EmptyProcessor();
    }

    @Override
    public LogEntry process(LogEntry logEntry) {
        return logEntry;
    }
}
