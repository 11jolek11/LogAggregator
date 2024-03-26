package org.personal.aggregation;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.personal.utils.LogEntry;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TransferQueue;

public class HTTPProducer extends Producer implements HttpHandler{
    public HTTPProducer(TransferQueue<LogEntry> queue, String name, Deserializer deserializer) {
        super(queue, name, deserializer);
    }

    @Override
    public Optional<LogEntry> deserialize(String serializedBodyString) {
        try {
            return Optional.ofNullable(this.deserializer.deserialize(serializedBodyString));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";

        String serializedString = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Optional<LogEntry> nextLogEntry = this.deserialize(serializedString);
        try {
            if (nextLogEntry.isPresent()) {
                produce(nextLogEntry.get());
            } else {
                response = "Malformed body";
                exchange.sendResponseHeaders(400, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
