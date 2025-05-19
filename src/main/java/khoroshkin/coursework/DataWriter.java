package khoroshkin.coursework;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class DataWriter {
    private final File outputFile;
    private final String outputFormat;
    private final ObjectMapper jsonMapper;
    private final CsvMapper csvMapper;
    private int idCounter = 1;
    private boolean isFirstWrite = true;


    public DataWriter(String outputFormat) {
        this.outputFormat = outputFormat;
        this.outputFile = new File("output.json");
        this.jsonMapper = new ObjectMapper();
        this.csvMapper = new CsvMapper();
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("{\n");
        } catch (IOException e) {
            // Logger
        }
    }

    public synchronized void saveData(String data, String serviceName) {
        try {
            if (outputFormat.equals("json")) {
                saveJsonData(data, serviceName);
            } else if (outputFormat.equals("csv")) {
                saveCsvData(data, serviceName);
            }
        } catch (IOException e) {
            // Logger
        }
    }


    private void saveJsonData(String data, String serviceName) throws IOException {
        JsonNode jsonData = jsonMapper.readTree(data);

        String fieldName = "response" + idCounter++ + "(" + serviceName + ")";

        ObjectNode rootNode;
        if (outputFile.length() == 0 || isFirstWrite) {
            rootNode = jsonMapper.createObjectNode();
        } else {
            rootNode = (ObjectNode) jsonMapper.readTree(outputFile);
        }

        rootNode.set(fieldName, jsonData);

        try (FileWriter writer = new FileWriter(outputFile)) {
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(writer, rootNode);
        }

        isFirstWrite = false;
    }

    private void saveCsvData(String data, String serviceName) throws IOException {
        saveJsonData(data, serviceName);
        File jsonFile = new File("output.json");

        JsonNode jsonTree = jsonMapper.readTree(jsonFile);

        ArrayNode responsesArray = jsonMapper.createArrayNode();
        Iterator<Map.Entry<String, JsonNode>> fields = jsonTree.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String responseId = field.getKey();
            JsonNode responseData = field.getValue();

            ObjectNode enrichedData = jsonMapper.createObjectNode();
            enrichedData.put("response_id", responseId);
            String extractedServiceName = responseId.substring(responseId.indexOf("(") + 1, responseId.length() - 1);
            enrichedData.put("service_name", extractedServiceName);

            Iterator<Map.Entry<String, JsonNode>> responseFields = responseData.fields();
            while (responseFields.hasNext()) {
                Map.Entry<String, JsonNode> responseField = responseFields.next();
                enrichedData.put(responseField.getKey(), responseField.getValue());
            }

            responsesArray.add(enrichedData);
        }


        Set<String> otherFields = new HashSet<>();
        for (JsonNode node : responsesArray) {
            node.fieldNames().forEachRemaining(fieldName -> {
                if (!fieldName.equals("response_id") && !fieldName.equals("service_name")) {
                    otherFields.add(fieldName);
                }
            });
        }

        CsvSchema.Builder csvSchemaBuilder = CsvSchema.builder();
        csvSchemaBuilder.addColumn("response_id");
        csvSchemaBuilder.addColumn("service_name");
        otherFields.forEach(csvSchemaBuilder::addColumn);
        CsvSchema csvSchema = csvSchemaBuilder.build().withHeader();

        csvMapper.writerFor(JsonNode.class)
                .with(csvSchema)
                .writeValue(new File("output.csv"), responsesArray);
    }
}
