package khoroshkin.coursework;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Logger logger = LogManager.getLogger(DataWriter.class);
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
            logger.info("Output file initialized: {}", outputFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to initialize output file", e);
        }
    }

    public synchronized void saveData(String data, String serviceName) {
        try {
            if ("json".equalsIgnoreCase(outputFormat)) {
                saveJsonData(data, serviceName);
            } else if ("csv".equalsIgnoreCase(outputFormat)){
                saveCsvData(data, serviceName);
            }
        } catch (IOException e) {
            logger.error("Failed to save data for service: {}", serviceName, e);
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

            ObjectNode flattenedData = jsonMapper.createObjectNode();
            flattenedData.put("response_id", responseId);
            String extractedServiceName = responseId.substring(responseId.indexOf("(") + 1, responseId.length() - 1);
            flattenedData.put("service_name", extractedServiceName);

            Iterator<Map.Entry<String, JsonNode>> responseFields = responseData.fields();
            while (responseFields.hasNext()) {
                Map.Entry<String, JsonNode> responseField = responseFields.next();
                String key = responseField.getKey();
                JsonNode value = responseField.getValue();

                if (value.isObject()) {
                    Iterator<Map.Entry<String, JsonNode>> nestedFields = value.fields();
                    while (nestedFields.hasNext()) {
                        Map.Entry<String, JsonNode> nestedField = nestedFields.next();
                        String nestedKey = key + "_" + nestedField.getKey();
                        flattenedData.put(nestedKey, nestedField.getValue().asText());
                    }
                } else {
                    flattenedData.put(key, value.asText());
                }
            }
            responsesArray.add(flattenedData);
        }

        Set<String> allFields = new TreeSet<>();
        for (JsonNode node : responsesArray) {
            node.fieldNames().forEachRemaining(allFields::add);
        }

        List<String> orderedColumns = new ArrayList<>();
        orderedColumns.add("response_id");
        orderedColumns.add("service_name");
        allFields.remove("response_id");
        allFields.remove("service_name");
        orderedColumns.addAll(allFields);

        CsvSchema.Builder csvSchemaBuilder = CsvSchema.builder();
        orderedColumns.forEach(csvSchemaBuilder::addColumn);
        CsvSchema csvSchema = csvSchemaBuilder.build().withHeader();

        csvMapper.writerFor(JsonNode.class)
                .with(csvSchema)
                .writeValue(new File("output.csv"), responsesArray);
    }
}
