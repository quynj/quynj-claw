package com.github.quynj.agentconsole.tool;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * Date and time tools for common date/time operations.
 */
@Service
public class DateTimeTools {

    @Tool(name = "current_time", description = "Get current date and time")
    public String currentTime(
            @ToolParam(name = "format", description = "DateTime format (e.g., yyyy-MM-dd HH:mm:ss). Default: yyyy-MM-dd HH:mm:ss") String format) {
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
    }

    @Tool(name = "current_date", description = "Get current date")
    public String currentDate(
            @ToolParam(name = "format", description = "Date format (e.g., yyyy-MM-dd). Default: yyyy-MM-dd") String format) {
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd";
        }
        return LocalDate.now().format(DateTimeFormatter.ofPattern(format));
    }

    @Tool(name = "current_timestamp", description = "Get current Unix timestamp (seconds since epoch)")
    public String currentTimestamp() {
        return String.valueOf(Instant.now().getEpochSecond());
    }

    @Tool(name = "current_millis", description = "Get current time in milliseconds since epoch")
    public String currentMillis() {
        return String.valueOf(System.currentTimeMillis());
    }

    @Tool(name = "timestamp_to_date", description = "Convert Unix timestamp to formatted date string")
    public String timestampToDate(
            @ToolParam(name = "timestamp", description = "Unix timestamp in seconds") long timestamp,
            @ToolParam(name = "format", description = "DateTime format. Default: yyyy-MM-dd HH:mm:ss") String format) {
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
        return dateTime.format(DateTimeFormatter.ofPattern(format));
    }

    @Tool(name = "date_to_timestamp", description = "Convert date string to Unix timestamp")
    public String dateToTimestamp(
            @ToolParam(name = "dateStr", description = "Date string") String dateStr,
            @ToolParam(name = "format", description = "Date format. Default: yyyy-MM-dd HH:mm:ss") String format) {
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(format));
            return String.valueOf(dateTime.atZone(ZoneId.systemDefault()).toEpochSecond());
        } catch (Exception e) {
            return "Error: Invalid date format or value - " + e.getMessage();
        }
    }

    @Tool(name = "date_diff", description = "Calculate difference between two dates")
    public String dateDiff(
            @ToolParam(name = "date1", description = "First date string") String date1,
            @ToolParam(name = "date2", description = "Second date string") String date2,
            @ToolParam(name = "unit", description = "Unit: days, hours, minutes, seconds. Default: days") String unit) {
        if (unit == null || unit.isEmpty()) {
            unit = "days";
        }

        try {
            LocalDateTime d1 = LocalDateTime.parse(date1, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime d2 = LocalDateTime.parse(date2, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            long diff;
            switch (unit.toLowerCase()) {
                case "days":
                    diff = ChronoUnit.DAYS.between(d1, d2);
                    break;
                case "hours":
                    diff = ChronoUnit.HOURS.between(d1, d2);
                    break;
                case "minutes":
                    diff = ChronoUnit.MINUTES.between(d1, d2);
                    break;
                case "seconds":
                    diff = ChronoUnit.SECONDS.between(d1, d2);
                    break;
                default:
                    return "Error: Invalid unit. Use: days, hours, minutes, seconds";
            }
            return String.valueOf(diff);
        } catch (Exception e) {
            return "Error parsing dates - " + e.getMessage();
        }
    }

    @Tool(name = "add_days", description = "Add days to a date")
    public String addDays(
            @ToolParam(name = "dateStr", description = "Date string (yyyy-MM-dd)") String dateStr,
            @ToolParam(name = "days", description = "Number of days to add (negative to subtract)") int days,
            @ToolParam(name = "format", description = "Output format. Default: yyyy-MM-dd") String format) {
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd";
        }
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate result = date.plusDays(days);
            return result.format(DateTimeFormatter.ofPattern(format));
        } catch (Exception e) {
            return "Error parsing date - " + e.getMessage();
        }
    }

    @Tool(name = "add_hours", description = "Add hours to a datetime")
    public String addHours(
            @ToolParam(name = "dateTimeStr", description = "DateTime string (yyyy-MM-dd HH:mm:ss)") String dateTimeStr,
            @ToolParam(name = "hours", description = "Number of hours to add (negative to subtract)") int hours,
            @ToolParam(name = "format", description = "Output format. Default: yyyy-MM-dd HH:mm:ss") String format) {
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime result = dateTime.plusHours(hours);
            return result.format(DateTimeFormatter.ofPattern(format));
        } catch (Exception e) {
            return "Error parsing datetime - " + e.getMessage();
        }
    }

    @Tool(name = "get_day_of_week", description = "Get the day of week for a date")
    public String getDayOfWeek(
            @ToolParam(name = "dateStr", description = "Date string (yyyy-MM-dd)") String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return date.getDayOfWeek().toString();
        } catch (Exception e) {
            return "Error parsing date - " + e.getMessage();
        }
    }

    @Tool(name = "is_weekend", description = "Check if a date is a weekend (Saturday or Sunday)")
    public String isWeekend(
            @ToolParam(name = "dateStr", description = "Date string (yyyy-MM-dd)") String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            DayOfWeek day = date.getDayOfWeek();
            return String.valueOf(day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
        } catch (Exception e) {
            return "Error parsing date - " + e.getMessage();
        }
    }

    @Tool(name = "get_first_day_of_month", description = "Get the first day of the month for a given date")
    public String getFirstDayOfMonth(
            @ToolParam(name = "dateStr", description = "Date string (yyyy-MM-dd)") String dateStr,
            @ToolParam(name = "format", description = "Output format. Default: yyyy-MM-dd") String format) {
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd";
        }
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate firstDay = date.with(TemporalAdjusters.firstDayOfMonth());
            return firstDay.format(DateTimeFormatter.ofPattern(format));
        } catch (Exception e) {
            return "Error parsing date - " + e.getMessage();
        }
    }

    @Tool(name = "get_last_day_of_month", description = "Get the last day of the month for a given date")
    public String getLastDayOfMonth(
            @ToolParam(name = "dateStr", description = "Date string (yyyy-MM-dd)") String dateStr,
            @ToolParam(name = "format", description = "Output format. Default: yyyy-MM-dd") String format) {
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd";
        }
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate lastDay = date.with(TemporalAdjusters.lastDayOfMonth());
            return lastDay.format(DateTimeFormatter.ofPattern(format));
        } catch (Exception e) {
            return "Error parsing date - " + e.getMessage();
        }
    }
}