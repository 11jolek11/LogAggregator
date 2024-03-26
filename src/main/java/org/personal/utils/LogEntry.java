package org.personal.utils;

import java.time.LocalDateTime;

public record LogEntry(LogLevel level, LocalDateTime dateTime, String message) {
}
