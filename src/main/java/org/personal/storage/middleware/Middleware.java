package org.personal.storage.middleware;

import org.personal.utils.LogEntry;

public class Middleware {

    private final MiddlewareProcessor currentMiddlewareProcessor;

    public Middleware(MiddlewareProcessor currentMiddlewareProcessor) {
        this.currentMiddlewareProcessor = currentMiddlewareProcessor;
    }

    Middleware addMiddlewareProcessor(MiddlewareProcessor newMiddlewareProcessor) {
        return new Middleware(input -> newMiddlewareProcessor.process(currentMiddlewareProcessor.process(input)));
    }

    public LogEntry process(LogEntry input) {
        if (!this.currentMiddlewareProcessor.canProcess(input)) {
            return input;
        }
        return this.currentMiddlewareProcessor.process(input);
    }
}
