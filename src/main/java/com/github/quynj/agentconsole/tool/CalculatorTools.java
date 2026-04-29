package com.github.quynj.agentconsole.tool;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.springframework.stereotype.Service;

/**
 * Calculator tools for basic mathematical operations.
 */
@Service
public class CalculatorTools {

    @Tool(name = "add", description = "Add two numbers")
    public String add(
            @ToolParam(name = "a", description = "First number") double a,
            @ToolParam(name = "b", description = "Second number") double b) {
        return String.valueOf(a + b);
    }

    @Tool(name = "subtract", description = "Subtract b from a")
    public String subtract(
            @ToolParam(name = "a", description = "First number") double a,
            @ToolParam(name = "b", description = "Second number") double b) {
        return String.valueOf(a - b);
    }

    @Tool(name = "multiply", description = "Multiply two numbers")
    public String multiply(
            @ToolParam(name = "a", description = "First number") double a,
            @ToolParam(name = "b", description = "Second number") double b) {
        return String.valueOf(a * b);
    }

    @Tool(name = "divide", description = "Divide a by b")
    public String divide(
            @ToolParam(name = "a", description = "First number (dividend)") double a,
            @ToolParam(name = "b", description = "Second number (divisor)") double b) {
        if (b == 0) {
            return "Error: Division by zero";
        }
        return String.valueOf(a / b);
    }

    @Tool(name = "power", description = "Calculate a raised to the power of b")
    public String power(
            @ToolParam(name = "a", description = "Base number") double a,
            @ToolParam(name = "b", description = "Exponent") double b) {
        return String.valueOf(Math.pow(a, b));
    }

    @Tool(name = "sqrt", description = "Calculate the square root of a number")
    public String sqrt(
            @ToolParam(name = "a", description = "The number to find square root of") double a) {
        if (a < 0) {
            return "Error: Cannot calculate square root of negative number";
        }
        return String.valueOf(Math.sqrt(a));
    }

    @Tool(name = "abs", description = "Calculate the absolute value of a number")
    public String abs(
            @ToolParam(name = "a", description = "The number") double a) {
        return String.valueOf(Math.abs(a));
    }

    @Tool(name = "floor", description = "Round down to the nearest integer")
    public String floor(
            @ToolParam(name = "a", description = "The number to floor") double a) {
        return String.valueOf((long) Math.floor(a));
    }

    @Tool(name = "ceil", description = "Round up to the nearest integer")
    public String ceil(
            @ToolParam(name = "a", description = "The number to ceil") double a) {
        return String.valueOf((long) Math.ceil(a));
    }

    @Tool(name = "round", description = "Round to the nearest integer")
    public String round(
            @ToolParam(name = "a", description = "The number to round") double a) {
        return String.valueOf(Math.round(a));
    }

    @Tool(name = "max", description = "Find the maximum of two numbers")
    public String max(
            @ToolParam(name = "a", description = "First number") double a,
            @ToolParam(name = "b", description = "Second number") double b) {
        return String.valueOf(Math.max(a, b));
    }

    @Tool(name = "min", description = "Find the minimum of two numbers")
    public String min(
            @ToolParam(name = "a", description = "First number") double a,
            @ToolParam(name = "b", description = "Second number") double b) {
        return String.valueOf(Math.min(a, b));
    }

    @Tool(name = "percentage", description = "Calculate percentage (a as percentage of b)")
    public String percentage(
            @ToolParam(name = "a", description = "The value") double a,
            @ToolParam(name = "b", description = "The total") double b) {
        if (b == 0) {
            return "Error: Total cannot be zero";
        }
        return String.format("%.2f%%", (a / b) * 100);
    }
}