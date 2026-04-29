package com.github.quynj.agentconsole.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "agent-console")
public class AgentConsoleProperties {
    public String baseDir;
    public String uiStorePath;
    public String agentscopeSessionStorePath;
    public String skillsPath;
    public String defaultAgentName = "Sunday";
    public String defaultSystemPrompt = "You are a helpful assistant.";
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
