package com.github.quynj.agentconsole.tool;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Locale;

/**
 * System information tools for retrieving environment and system details.
 */
@Service
public class SystemInfoTools {

    @Tool(name = "get_system_info", description = "Get basic system information including OS, Java version, timezone, etc.")
    public String getSystemInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== System Information ===\n");
        sb.append("OS Name: ").append(System.getProperty("os.name")).append("\n");
        sb.append("OS Version: ").append(System.getProperty("os.version")).append("\n");
        sb.append("OS Architecture: ").append(System.getProperty("os.arch")).append("\n");
        sb.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        sb.append("Java Vendor: ").append(System.getProperty("java.vendor")).append("\n");
        sb.append("Java Home: ").append(System.getProperty("java.home")).append("\n");
        sb.append("Timezone: ").append(ZoneId.systemDefault()).append("\n");
        sb.append("Default Locale: ").append(Locale.getDefault()).append("\n");
        sb.append("User Name: ").append(System.getProperty("user.name")).append("\n");
        sb.append("User Home: ").append(System.getProperty("user.home")).append("\n");
        sb.append("Current Directory: ").append(System.getProperty("user.dir")).append("\n");
        return sb.toString();
    }

    @Tool(name = "get_system_property", description = "Get a specific Java system property by key")
    public String getSystemProperty(
            @ToolParam(name = "key", description = "The system property key (e.g., 'os.name', 'java.version')") String key) {
        if (key == null || key.isEmpty()) {
            return "Error: Property key cannot be empty";
        }
        String value = System.getProperty(key);
        if (value == null) {
            return "Property '" + key + "' not found";
        }
        return key + " = " + value;
    }

    @Tool(name = "list_system_properties", description = "List all available Java system properties")
    public String listSystemProperties() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== System Properties ===\n");
        System.getProperties().forEach((k, v) -> sb.append(k).append(" = ").append(v).append("\n"));
        return sb.toString();
    }

    @Tool(name = "get_environment_variable", description = "Get a specific environment variable by name")
    public String getEnvironmentVariable(
            @ToolParam(name = "name", description = "The environment variable name") String name) {
        if (name == null || name.isEmpty()) {
            return "Error: Variable name cannot be empty";
        }
        String value = System.getenv(name);
        if (value == null) {
            return "Environment variable '" + name + "' not found";
        }
        return name + " = " + value;
    }

    @Tool(name = "get_memory_info", description = "Get JVM memory usage information")
    public String getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        StringBuilder sb = new StringBuilder();
        sb.append("=== Memory Information ===\n");
        sb.append("Max Memory: ").append(formatBytes(maxMemory)).append("\n");
        sb.append("Total Memory: ").append(formatBytes(totalMemory)).append("\n");
        sb.append("Free Memory: ").append(formatBytes(freeMemory)).append("\n");
        sb.append("Used Memory: ").append(formatBytes(usedMemory)).append("\n");
        sb.append("Memory Usage: ").append(String.format("%.2f%%", (double) usedMemory / maxMemory * 100)).append("\n");
        return sb.toString();
    }

    /**
     * Format bytes to human readable format
     *
     * @param bytes bytes to format
     * @return formatted string
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
