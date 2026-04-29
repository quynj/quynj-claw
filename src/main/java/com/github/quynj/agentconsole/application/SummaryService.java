package com.github.quynj.agentconsole.application;

import com.github.quynj.agentconsole.dto.SessionSummaryDTO;
import com.github.quynj.agentconsole.store.LocalSummaryStore;
import org.springframework.stereotype.Service;

@Service
public class SummaryService {
    private final LocalSummaryStore store;

    public SummaryService(LocalSummaryStore store) {
        this.store = store;
    }

    public SessionSummaryDTO get(String sessionId) {
        SessionSummaryDTO summary = store.get(sessionId);
        if (summary == null) {
            throw new IllegalArgumentException("Summary not found: " + sessionId);
        }
        return summary;
    }
}
