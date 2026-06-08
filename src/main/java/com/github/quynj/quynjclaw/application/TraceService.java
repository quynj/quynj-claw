package com.github.quynj.quynjclaw.application;

import com.github.quynj.quynjclaw.common.IdGenerator;
import com.github.quynj.quynjclaw.dto.ChatSessionDTO;
import com.github.quynj.quynjclaw.dto.TraceSpanDTO;
import com.github.quynj.quynjclaw.store.LocalSessionStore;
import com.github.quynj.quynjclaw.store.LocalSummaryStore;
import com.github.quynj.quynjclaw.store.LocalTraceStore;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TraceService {
    private final LocalTraceStore traceStore;
    private final LocalSessionStore sessionStore;
    private final LocalSummaryStore summaryStore;
    private final RealtimeEventService realtimeEventService;

    public TraceService(LocalTraceStore traceStore,
                        LocalSessionStore sessionStore,
                        LocalSummaryStore summaryStore,
                        RealtimeEventService realtimeEventService) {
        this.traceStore = traceStore;
        this.sessionStore = sessionStore;
        this.summaryStore = summaryStore;
        this.realtimeEventService = realtimeEventService;
    }

    public TraceSpanDTO begin(String sessionId, String parentSpanId, String name, String spanType,
                              Object input, Map<String, Object> metadata) {
        TraceSpanDTO span = new TraceSpanDTO();
        span.id = IdGenerator.traceId();
        span.sessionId = sessionId;
        span.parentSpanId = parentSpanId;
        span.name = name;
        span.spanType = spanType;
        span.status = "running";
        span.input = input;
        span.metadata = metadata == null ? new LinkedHashMap<>() : new LinkedHashMap<>(metadata);
        span.startedAt = LocalDateTime.now();
        traceStore.append(sessionId, span);
        ChatSessionDTO session = sessionStore.incrementTraceCount(sessionId);
        summaryStore.updateAfterSession(session);
        realtimeEventService.publishTraceCreated(sessionId, span);
        realtimeEventService.publishSessionUpdated(sessionId, session);
        return span;
    }

    public TraceSpanDTO recordSuccess(String sessionId, String parentSpanId, String name, String spanType,
                                      Object input, Object output, Map<String, Object> metadata) {
        TraceSpanDTO span = begin(sessionId, parentSpanId, name, spanType, input, metadata);
        return finishSuccess(span, output, null);
    }

    public TraceSpanDTO finishSuccess(TraceSpanDTO span, Object output, Map<String, Object> metadata) {
        return finish(span, "success", output, metadata);
    }

    public TraceSpanDTO finishError(TraceSpanDTO span, Object output, Map<String, Object> metadata) {
        return finish(span, "error", output, metadata);
    }

    public TraceSpanDTO finishCancelled(TraceSpanDTO span, Object output, Map<String, Object> metadata) {
        return finish(span, "cancelled", output, metadata);
    }

    private TraceSpanDTO finish(TraceSpanDTO span, String status, Object output, Map<String, Object> metadata) {
        if (span == null) {
            return null;
        }
        span.status = status;
        span.output = output;
        if (metadata != null) {
            span.metadata.putAll(metadata);
        }
        span.endedAt = LocalDateTime.now();
        span.durationMs = Duration.between(span.startedAt, span.endedAt).toMillis();
        traceStore.update(span.sessionId, span);
        realtimeEventService.publishTraceUpdated(span.sessionId, span);
        return span;
    }
}
