package org.personal.utils;

import java.time.LocalDateTime;

public record LogEntry(String device, LogLevel level, LocalDateTime dateTime, String message) {
}
