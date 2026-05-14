package com.resourcemanager.transfer.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TsvParser {

    public static List<Map<String, String>> parse(InputStream input, String[] expectedHeaders) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return rows;

            if (headerLine.startsWith("#")) {
                headerLine = headerLine.substring(1);
            }
            String[] headers = headerLine.split("\t", -1);

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] values = line.split("\t", -1);
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i].trim(), values[i]);
                }
                rows.add(row);
            }
        }
        return rows;
    }

    public static byte[] generate(String[] headers, List<Map<String, String>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("#").append(String.join("\t", headers)).append("\n");
        for (Map<String, String> row : rows) {
            String[] values = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                values[i] = row.getOrDefault(headers[i], "");
            }
            sb.append(String.join("\t", values)).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static List<String> validate(List<Map<String, String>> rows, String[] requiredFields) {
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            for (String field : requiredFields) {
                String value = row.get(field);
                if (value == null || value.isBlank()) {
                    errors.add("Row " + (i + 1) + ": missing required field '" + field + "'");
                }
            }
        }
        return errors;
    }
}
