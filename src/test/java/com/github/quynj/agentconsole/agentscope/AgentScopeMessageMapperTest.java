package com.github.quynj.agentconsole.agentscope;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ThinkingBlock;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AgentScopeMessageMapperTest {
    private final AgentScopeMessageMapper mapper = new AgentScopeMessageMapper(JsonMapper.builder().build());

    @Test
    void preservesThinkingBlocksAndMetadata() {
        Msg msg = Msg.builder()
                .name("Sunday")
                .role(MsgRole.ASSISTANT)
                .content(List.of(
                        ThinkingBlock.builder()
                                .thinking("先检查记忆文件路径。")
                                .metadata(Map.of("source", "model"))
                                .build(),
                        TextBlock.builder()
                                .text("文件已经放到正确目录。")
                                .build()))
                .metadata(Map.of("_chat_usage", Map.of("inputTokens", 7, "outputTokens", 3)))
                .build();

        var projection = mapper.toAssistantProjection("sess_1", "Sunday", msg);

        assertEquals(2, projection.content.size());
        assertEquals("thinking", projection.content.get(0).type);
        assertEquals("先检查记忆文件路径。", projection.content.get(0).thinking);
        assertEquals(Map.of("source", "model"), projection.content.get(0).metadata);
        assertEquals("text", projection.content.get(1).type);
        assertEquals("文件已经放到正确目录。", projection.content.get(1).text);
        assertNotNull(projection.metadata.get("_chat_usage"));
    }
}
