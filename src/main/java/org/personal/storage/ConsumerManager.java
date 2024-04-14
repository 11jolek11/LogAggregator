package org.personal.storage;

import org.personal.aggregation.MQTTProducer;
import org.personal.utils.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

public class ConsumerManager<T extends Consumer> {

    private final TransferQueue<LogEntry> targetQueue;
    private Map<String, T> availableConsumers;
    private final int maxPoolSize;
    private ExecutorService threadPool;
    private Map<String, Future<?>> consumersTrack;
    private final Logger LOGGER = LoggerFactory.getLogger(ConsumerManager.class);

    public ConsumerManager(TransferQueue<LogEntry> targetQueue, Collection<T> consumers, int maxPoolSize) {
        Iterator<T> iter;
        this.targetQueue = targetQueue;
        this.maxPoolSize = maxPoolSize;
        consumersTrack = new HashMap<>(maxPoolSize);

        if (consumers.size() <= maxPoolSize) {
            this.availableConsumers = new HashMap<>(consumers.size());
            iter = consumers.iterator();
        } else {
            throw new IllegalArgumentException("Consumer pool size must be less than or equal to " + maxPoolSize);
        }

        while (iter.hasNext()) {
            T consumer = iter.next();
            this.availableConsumers.put(consumer.getName(), consumer);
        }

        this.threadPool = Executors.newFixedThreadPool(this.maxPoolSize);
    }

    public ConsumerManager(TransferQueue<LogEntry> targetQueue, int maxPoolSize) {
        this(targetQueue, new HashSet<>(), maxPoolSize);
    }

    public void startAllConsumers() {
        for (T consumer : availableConsumers.values()) {
            this.consumersTrack.put(consumer.getName(), threadPool.submit(consumer));
        }
    }

    public void shutdown(String consumerName) {
        this.consumersTrack.remove(consumerName).cancel(true);
    }

    public void start(String consumerName) {
        Consumer consumer = this.availableConsumers.get(consumerName);
        if (consumer == null) {
            throw new IllegalArgumentException("Consumer " + consumerName + " not found");
        }
        if (!consumersTrack.containsKey(consumerName)) {
            this.consumersTrack.put(consumer.getName(), threadPool.submit(consumer));
        } else {
            System.out.println("Consumer " + consumerName + " is already running");
        }
    }

    public void shutdownAllConsumers() {
        this.threadPool.shutdown();
        this.consumersTrack.clear();
    }

    public void addConsumer(T newConsumer) {
        this.availableConsumers.put(newConsumer.getName(), newConsumer);
    }

    public Set<String> getAvailableConsumers() {
        return availableConsumers.keySet();
    }
}
