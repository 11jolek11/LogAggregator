package org.personal.storage;

import org.personal.storage.middleware.Middleware;
import org.personal.utils.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.TransferQueue;

public class SQLConsumer extends Consumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLConsumer.class);
    private HashMap<String, String> preparedStatement = new HashMap<>();
    private HashMap<String, String> callableStatement = new HashMap<>();

    private final Connection connection;

    public SQLConsumer(TransferQueue<LogEntry> queue, Middleware middleware, String storeStatement, Connection connection) {
        super(queue, middleware);
        this.connection = connection;
        this.preparedStatement.put("storeStatement", storeStatement);
    }

    private SQLConsumer(SQLConsumer sqlConsumer) {
        super(sqlConsumer);
        this.connection = sqlConsumer.connection;
        this.preparedStatement = sqlConsumer.preparedStatement;
        this.callableStatement = sqlConsumer.callableStatement;
    }

//    public void <T> T executePreparedStatement(String preparedStatementName,) throws SQLException {
//        try (PreparedStatement pstm = this.connection.prepareStatement(this.preparedStatement.get("storeStatement"))) {
//            pstm.setString(0, );
//        }
//    }
//
//    public void executeCallableStatement(String callableStatementName) {
//
//    }

    public HashMap<String, String> getPreparedStatement() {
        return preparedStatement;
    }

    public void setPreparedStatement(HashMap<String, String> preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    public HashMap<String, String> getCallableStatement() {
        return callableStatement;
    }

    public void setCallableStatement(HashMap<String, String> callableStatement) {
        this.callableStatement = callableStatement;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void consume() {
        var consumedLog = this.getQueue().poll();
        if (consumedLog == null) {
            return;
        }

        var processedLog = this.getMiddleware().process(consumedLog);
        this.save(processedLog);
    }

    @Override
    public void save(LogEntry logEntry) {
        try (PreparedStatement pstm = this.connection.prepareStatement(this.preparedStatement.get("storeStatement"))) {
            pstm.setString(0, logEntry.device());
            pstm.setString(1, logEntry.level().name());
            pstm.setString(2, String.valueOf(logEntry.dateTime()));
            pstm.setString(3, logEntry.message());
        } catch (SQLException e) {
            LOGGER.error("SQL Error: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        while (true) {
            this.consume();
        }
    }

    @Override
    public Consumer clone() {
        return new SQLConsumer(this);
    }
}
