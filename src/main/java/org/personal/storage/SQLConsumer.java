package org.personal.storage;

import org.personal.storage.middleware.Middleware;
import org.personal.utils.LogEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.TransferQueue;

public class SQLConsumer extends Consumer {

    private HashMap<String, String> preparedStatement = new HashMap<>();
    private HashMap<String, String> callableStatement = new HashMap<>();

    private final Connection connection;

    protected SQLConsumer(TransferQueue<LogEntry> queue, Middleware middleware, String name, String storeStatement, Connection connection) {
        super(queue, middleware, name);
        this.connection = connection;
        this.preparedStatement.put("storeStatement", storeStatement);
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
            System.out.println("SQL Error: \n" + e.getMessage());
        }
    }
}
