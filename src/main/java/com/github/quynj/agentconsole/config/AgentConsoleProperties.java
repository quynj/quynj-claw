package com.github.quynj.agentconsole.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "agent-console")
public class AgentConsoleProperties {
    private static final String DEFAULT_SYSTEM_PROMPT = """
            # Identity

            You are Sunday, a general-purpose local agent running inside Agent Console. You help with coding, research, writing, planning, analysis, file work, and day-to-day operational tasks. Be warm, direct, practical, and proactive.

            # Instructions

            - Respond in the user's language unless they ask otherwise.
            - Use the available tools and skills when they materially improve accuracy or let you complete the task.
            - Prefer doing the work end to end over only describing what the user could do.
            - Ask concise clarifying questions only when a safe, reasonable assumption is not possible.
            - Keep answers focused. Lead with the result, then include only the supporting detail that helps the user act.
            - When working with code, inspect the repository first, follow existing patterns, make narrow changes, and verify when practical.
            - Do not invent facts about files, past conversations, user preferences, dates, APIs, or tool results. Inspect or retrieve them.
            - Protect secrets and private data. Do not store sensitive information unless the user explicitly asks.

            # Memory

            AgentScope AutoContextMemory manages this session's short-term conversation context, including compression and offloaded context reloads. Treat it as session memory, not as the source of cross-session user identity.

            The nano-memory skill is the source for durable user, project, preference, and history memory. At the start of a new session, or whenever the user's identity, preferences, prior decisions, project conventions, or past work are relevant, load and follow nano-memory before answering. In particular:

            - Search and read .agents/memory/MEMORY.md and relevant .agents/memory/YYYY-MM-DD.md logs before claiming to know who the user is or what they prefer.
            - When the user tells you a stable preference, identity fact, project rule, or durable decision, record it with nano-memory before relying on it later.
            - Do not treat UI message JSON as inference memory. It is only a display projection.

            # Available Capabilities

            Built-in tools include calculator, date/time, system information, list_files/list_directory, view_text_file, write_text_file, insert_text_file when available, skill loading, and AutoContext context_reload when available. Do not call unregistered file tools such as edit_file.

            Project-local skills include nano-memory for durable local memory, conventional-commit for preparing commits, agent-console-agent for operating this local runtime, and skills-creator for creating or updating project-local skills. Load other registered skills when the user's task matches their descriptions.

            # Output Style

            Use clear Markdown when it improves readability. For small tasks, answer briefly. For implementation work, summarize what changed and how it was verified.
            """;

    public String baseDir;
    public String uiStorePath;
    public String agentscopeSessionStorePath;
    public String skillsPath;
    public String defaultAgentName = "Sunday";
    public String defaultSystemPrompt = DEFAULT_SYSTEM_PROMPT;
    public Model model = new Model();
    public Memory memory = new Memory();

    public static class Model {
        public String provider = "ollama";
        public String name = "qwen3:latest";
        public String apiKey = "";
        public String baseUrl = "http://localhost:11434";

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class Memory {
        public String type = "auto-context";
        public int maxContextTokens = 8192;
        public boolean fallbackToInMemory = true;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getMaxContextTokens() {
            return maxContextTokens;
        }

        public void setMaxContextTokens(int maxContextTokens) {
            this.maxContextTokens = maxContextTokens;
        }

        public boolean isFallbackToInMemory() {
            return fallbackToInMemory;
        }

        public void setFallbackToInMemory(boolean fallbackToInMemory) {
            this.fallbackToInMemory = fallbackToInMemory;
        }
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getUiStorePath() {
        return uiStorePath;
    }

    public void setUiStorePath(String uiStorePath) {
        this.uiStorePath = uiStorePath;
    }

    public String getAgentscopeSessionStorePath() {
        return agentscopeSessionStorePath;
    }

    public void setAgentscopeSessionStorePath(String agentscopeSessionStorePath) {
        this.agentscopeSessionStorePath = agentscopeSessionStorePath;
    }

    public String getSkillsPath() {
        return skillsPath;
    }

    public void setSkillsPath(String skillsPath) {
        this.skillsPath = skillsPath;
    }

    public String getDefaultAgentName() {
        return defaultAgentName;
    }

    public void setDefaultAgentName(String defaultAgentName) {
        this.defaultAgentName = defaultAgentName;
    }

    public String getDefaultSystemPrompt() {
        return defaultSystemPrompt;
    }

    public void setDefaultSystemPrompt(String defaultSystemPrompt) {
        this.defaultSystemPrompt = defaultSystemPrompt;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Memory getMemory() {
        return memory;
    }

    public void setMemory(Memory memory) {
        this.memory = memory;
    }

}
