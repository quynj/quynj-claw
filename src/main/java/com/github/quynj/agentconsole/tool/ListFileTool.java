package com.github.quynj.agentconsole.tool;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ListFileTool {

    private final Path safeBasePath;

    /**
     * Constructor with safe path restriction
     *
     * @param safePath the base path for safe file operations
     */
    public ListFileTool(String safePath) {
        this.safeBasePath = Paths.get(safePath).toAbsolutePath().normalize();
    }

    /**
     * Default constructor for Spring bean registration
     */
    public ListFileTool() {
        this(System.getProperty("user.dir"));
    }

    @Tool(name = "list_files", description = "List files in a directory")
    public String listFiles(
            @ToolParam(name = "directory", description = "Directory path") String directory) {
        try {
            Path dir = resolveSafePath(directory);
            if (dir == null) {
                return "Error: Access denied. Path is outside the allowed directory: " + safeBasePath;
            }

            if (!Files.isDirectory(dir)) {
                return "Error: Not a directory: " + directory;
            }

            StringBuilder result = new StringBuilder("Files in ").append(dir).append(":\n");
            try (var stream = Files.list(dir)) {
                stream.forEach(path -> result.append("  - ").append(path.getFileName()).append("\n"));
            }
            return result.toString();
        } catch (IOException e) {
            return "Error listing directory: " + e.getMessage();
        }
    }

    /**
     * Resolve path and ensure it's within the safe base path
     *
     * @param path the requested path
     * @return the resolved absolute path, or null if outside safe bounds
     */
    private Path resolveSafePath(String path) {
        try {
            Path resolved = Paths.get(path).toAbsolutePath().normalize();
            Path normalizedBase = safeBasePath.normalize();

            if (resolved.startsWith(normalizedBase)) {
                return resolved;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
