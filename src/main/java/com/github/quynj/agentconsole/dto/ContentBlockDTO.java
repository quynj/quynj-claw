package com.github.quynj.agentconsole.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentBlockDTO {
    public String type;
    public String text;
    public String id;
    public String name;
    public Map<String, Object> input;
    public String toolUseId;
    public Object output;
    public Boolean isError;
    public String message;
    public String detail;
    public String url;
    public String alt;

    public static ContentBlockDTO text(String text) {
        ContentBlockDTO block = new ContentBlockDTO();
        block.type = "text";
        block.text = text;
        return block;
    }

    public static ContentBlockDTO error(String message, String detail) {
        ContentBlockDTO block = new ContentBlockDTO();
        block.type = "error";
        block.message = message;
        block.detail = detail;
        return block;
    }

    public Map<String, Object> inputOrEmpty() {
        if (input == null) {
            input = new LinkedHashMap<>();
        }
        return input;
    }
}
