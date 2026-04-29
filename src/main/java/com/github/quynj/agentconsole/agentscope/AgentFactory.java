package com.github.quynj.agentconsole.agentscope;

import com.github.quynj.agentconsole.config.AgentConsoleProperties;
import com.github.quynj.agentconsole.dto.ChatSessionDTO;
import com.github.quynj.agentconsole.tool.CalculatorTools;
import com.github.quynj.agentconsole.tool.DateTimeTools;
import com.github.quynj.agentconsole.tool.ListFileTool;
import com.github.quynj.agentconsole.tool.SystemInfoTools;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.Memory;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OllamaChatModel;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.skill.repository.FileSystemSkillRepository;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.file.ReadFileTool;
import io.agentscope.core.tool.file.WriteFileTool;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class AgentFactory {
    private final AgentConsoleProperties properties;
    private final AgentMemoryFactory memoryFactory;
    private final CalculatorTools calculatorTools;
    private final DateTimeTools dateTimeTools;
    private final ListFileTool listFileTool;
    private final SystemInfoTools systemInfoTools;

    public AgentFactory(AgentConsoleProperties properties,
                        AgentMemoryFactory memoryFactory,
                        CalculatorTools calculatorTools,
                        DateTimeTools dateTimeTools,
                        ListFileTool listFileTool,
                        SystemInfoTools systemInfoTools) {
        this.properties = properties;
        this.memoryFactory = memoryFactory;
        this.calculatorTools = calculatorTools;
        this.dateTimeTools = dateTimeTools;
        this.listFileTool = listFileTool;
        this.systemInfoTools = systemInfoTools;
    }

    public ReActAgent createAgent(ChatSessionDTO session) {
        Model model = createModel(session);
        Memory memory = memoryFactory.createMemory(session, model);
        Toolkit toolkit = createToolkit();
        SkillBox skillBox = createSkillBox(toolkit);
        /*
         * Session isolation: every request creates a fresh ReActAgent and a fresh Memory instance.
         * Only AgentScope JsonSession for the current agentscopeSessionId is loaded before call()
         * and saved after call(). UI messages are never replayed into the prompt here.
         * Built-in tools and project-local skills are stateless registrations on this per-call agent.
         */
        return ReActAgent.builder()
                .name(session.agentName)
                .sysPrompt(session.systemPrompt)
                .model(model)
                .memory(memory)
                .toolkit(toolkit)
                .skillBox(skillBox)
                .build();
    }

    private String getProjectRootPath() {
        return Paths.get("").toAbsolutePath().toString();
    }

    private Toolkit createToolkit() {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(calculatorTools);
        toolkit.registerTool(dateTimeTools);
        toolkit.registerTool(listFileTool);
        toolkit.registerTool(systemInfoTools);
        toolkit.registerTool(new WriteFileTool(getProjectRootPath()));
        toolkit.registerTool(new ReadFileTool(getProjectRootPath()));

        return toolkit;
    }

    private SkillBox createSkillBox(Toolkit toolkit) {
        SkillBox skillBox = new SkillBox(toolkit);
        Path skillsPath = Path.of(properties.skillsPath);
        if (!Files.isDirectory(skillsPath)) {
            return skillBox;
        }
        FileSystemSkillRepository repository = new FileSystemSkillRepository(skillsPath, false, "project-local");
        repository.getAllSkills().forEach(skillBox::registerSkill);
        skillBox.registerSkillLoadTool();
        return skillBox;
    }

    private Model createModel(ChatSessionDTO session) {
        String provider = properties.model.provider == null ? "ollama" : properties.model.provider.toLowerCase();
        String modelName = session.modelName == null || session.modelName.isBlank() ? properties.model.name : session.modelName;
        return switch (provider) {
            case "dashscope" -> {
                requireApiKey("DASHSCOPE_API_KEY", properties.model.apiKey);
                yield DashScopeChatModel.builder()
                        .apiKey(properties.model.apiKey)
                        .modelName(modelName)
                        .build();
            }
            case "openai" -> {
                requireApiKey("OPENAI_API_KEY", properties.model.apiKey);
                yield OpenAIChatModel.builder()
                        .apiKey(properties.model.apiKey)
                        .modelName(modelName)
                        .baseUrl(properties.model.baseUrl)
                        .build();
            }
            case "ollama" -> OllamaChatModel.builder()
                    .modelName(modelName)
                    .baseUrl(properties.model.baseUrl)
                    .build();
            default -> throw new IllegalStateException("Unsupported model provider: " + provider);
        };
    }

    private void requireApiKey(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is not configured.");
        }
    }
}
