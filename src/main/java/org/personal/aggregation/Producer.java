package org.personal.aggregation;

import org.personal.utils.LogEntry;
import org.personal.utils.LogLevel;

import java.util.Optional;
import java.util.concurrent.TransferQueue;

public abstract class Producer {
    private final TransferQueue<LogEntry> queue;
    private final String name;
    protected final Deserializer deserializer;

    public Producer(TransferQueue<LogEntry> queue, String name, Deserializer deserializer) {
        this.queue = queue;
        this.name = name;
        this.deserializer = deserializer;
    }

    public synchronized void produce(LogEntry logEntry) throws InterruptedException{
        if ((logEntry.level() == LogLevel.ERROR) || (logEntry.level() == LogLevel.WARNING)) {
            this.queue.add(logEntry);
        } else {
            this.queue.tryTransfer(logEntry);
        }
    }

    public TransferQueue<LogEntry> getQueue() {
        return queue;
    }

    public String getName() {
        return name;
    }

    public abstract Optional<LogEntry> deserialize(String serializedBodyString);
}
