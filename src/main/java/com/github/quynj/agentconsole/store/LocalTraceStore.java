package com.github.quynj.agentconsole.store;

import com.github.quynj.agentconsole.common.JsonFileUtils;
import com.github.quynj.agentconsole.config.AgentConsoleProperties;
import com.github.quynj.agentconsole.dto.TraceSpanDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class LocalTraceStore {
    private final AgentConsoleProperties properties;
    private final JsonFileUtils json;
    private Path dir;

    public LocalTraceStore(AgentConsoleProperties properties, ObjectMapper mapper) {
        this.properties = properties;
        this.json = new JsonFileUtils(mapper);
    }

    @PostConstruct
    public void init() throws IOException {
        dir = Path.of(properties.uiStorePath, "traces");
        Files.createDirectories(dir);
    }

    public synchronized void initSession(String sessionId) {
        json.writeAtomic(path(sessionId), new Document(sessionId));
    }

    public synchronized List<TraceSpanDTO> list(String sessionId) {
        return read(sessionId).items;
    }

    public synchronized Optional<TraceSpanDTO> get(String traceId) {
        try (var paths = Files.list(dir)) {
            return paths.filter(path -> path.toString().endsWith(".json"))
                    .map(path -> json.read(path, Document.class, new Document()))
                    .flatMap(doc -> doc.items.stream())
                    .filter(item -> Objects.equals(item.id, traceId))
                    .findFirst();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan traces", e);
        }
    }

    public synchronized void append(String sessionId, TraceSpanDTO span) {
        Document doc = read(sessionId);
        doc.items.add(span);
        json.writeAtomic(path(sessionId), doc);
    }

    public synchronized void deleteBySessionId(String sessionId) {
        try {
            Files.deleteIfExists(path(sessionId));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete traces for " + sessionId, e);
        }
    }

    private Document read(String sessionId) {
        return json.read(path(sessionId), Document.class, new Document(sessionId));
    }

    private Path path(String sessionId) {
        return dir.resolve(sessionId + ".json");
    }

    public static class Document {
        public String sessionId;
        public List<TraceSpanDTO> items = new ArrayList<>();

        public Document() {
        }

        public Document(String sessionId) {
            this.sessionId = sessionId;
        }
    }
}
