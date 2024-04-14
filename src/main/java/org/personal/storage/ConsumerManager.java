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
        this.consumersTrack.clear();
        for (T consumer : availableConsumers.values()) {
            this.consumersTrack.put(consumer.getName(), threadPool.submit(consumer));
        }

        this.LOGGER.info("Consumer started: {}", this.consumersTrack.values().toString());
    }

    public void shutdown(String consumerName) {
        this.consumersTrack.remove(consumerName).cancel(true);
        this.LOGGER.info("Consumer closed: {}", consumerName);
    }

    public void start(String consumerName) {
        Consumer consumer = this.availableConsumers.get(consumerName);
        if (consumer == null) {
            throw new IllegalArgumentException("Consumer " + consumerName + " not found");
        }
        if (!consumersTrack.containsKey(consumerName)) {
            this.consumersTrack.put(consumer.getName(), threadPool.submit(consumer));
            this.LOGGER.info("Consumer started: {}", consumerName);
        } else {
            this.LOGGER.info("Consumer {} is already running", consumerName);
        }
    }

    public void shutdownAllConsumers() {
        this.threadPool.shutdown();
        Set<String> tempConsumers = this.consumersTrack.keySet();
        this.consumersTrack.clear();
        this.LOGGER.info("Consumer closed: {}", tempConsumers.toString());
    }

    public void addConsumer(T newConsumer) {
        this.availableConsumers.put(newConsumer.getName(), newConsumer);
        this.LOGGER.info("Consumer added: {}", newConsumer.toString());
    }

    public Set<String> getAvailableConsumers() {
        return availableConsumers.keySet();
    }
}
