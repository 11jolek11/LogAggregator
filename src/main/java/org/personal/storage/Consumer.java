package org.personal.storage;

import org.personal.storage.middleware.EmptyProcessor;
import org.personal.storage.middleware.Middleware;
import org.personal.utils.LogEntry;

import java.util.concurrent.TransferQueue;

// TODO(11jolek11): Refactor!!!
public abstract class Consumer {
    private final TransferQueue<LogEntry> queue;
    protected Middleware middleware;
    private final String name;

    protected Consumer(TransferQueue<LogEntry> queue, Middleware middleware, String name) {
        this.queue = queue;
        this.middleware = middleware;
        this.name = name;
    }

    protected Consumer(TransferQueue<LogEntry> queue, String name) {
        this(queue, new Middleware(EmptyProcessor.createEmptyProcessor()), name);
    }

    public void setMiddleware(Middleware middleware) {
        this.middleware = middleware;
    }

    public Middleware getMiddleware() {
        return middleware;
    }

    public TransferQueue<LogEntry> getQueue() {
        return queue;
    }

    public String getName() {
        return name;
    }

//    public abstract void consume();

    public abstract void consume();

// TODO(11jolek11): New Idea!
//    https://stackoverflow.com/questions/44003681/how-can-i-process-all-elements-of-a-queue-without-blocking-using-streams

    public abstract void save(LogEntry logEntry);
}
