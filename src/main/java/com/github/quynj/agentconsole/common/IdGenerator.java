package com.github.quynj.agentconsole.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

public final class IdGenerator {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.ROOT);

    private IdGenerator() {
    }

    public static String sessionId() {
        return "sess_" + TS.format(LocalDateTime.now()) + "_" + shortRandom();
    }

    public static String messageId() {
        return "msg_" + TS.format(LocalDateTime.now()) + "_" + shortRandom();
    }

    public static String traceId() {
        return "trace_" + TS.format(LocalDateTime.now()) + "_" + shortRandom();
    }

    private static String shortRandom() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
