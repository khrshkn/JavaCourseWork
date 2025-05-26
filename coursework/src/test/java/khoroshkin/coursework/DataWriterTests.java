package khoroshkin.coursework;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataWriterTests {
    private static final ObjectMapper JSON = new ObjectMapper();
    private Path projectRoot;
    private Path jsonPath;
    private Path csvPath;

    @BeforeEach
    void configPaths() {
        projectRoot = Paths.get("").toAbsolutePath();
        jsonPath = projectRoot.resolve("output.json");
        csvPath = projectRoot.resolve("output.csv");
    }

    @Test
    void saveData_inJson() throws IOException {
        DataWriter writer = new DataWriter("json");

        writer.saveData("{\"foo\":1}", "SvcA");
        writer.saveData("{\"bar\":\"baz\"}", "SvcB");

        assertTrue(Files.exists(jsonPath));

        JsonNode root = JSON.readTree(jsonPath.toFile());
        assertTrue(root.has("response1(SvcA)"));
        assertTrue(root.has("response2(SvcB)"));
        assertEquals(1, root.get("response1(SvcA)").get("foo").intValue());
        assertEquals("baz", root.get("response2(SvcB)").get("bar").textValue());
    }

    @Test
    void saveData_inCsv() throws IOException {
        DataWriter writer = new DataWriter("csv");
        writer.saveData("{\"a\":10}", "Svc1");
        writer.saveData("{\"b\":5}", "Svc2");

        Path csvPath = Paths.get("output.csv");

        assertTrue(Files.exists(csvPath));
        List<String> lines = Files.readAllLines(csvPath);
        assertFalse(lines.isEmpty());

        String header = lines.get(0);
        assertTrue(header.contains("response_id"));
        assertTrue(header.contains("service_name"));
        assertTrue(header.contains("a"));
        assertTrue(header.contains("b"));

        String[] cols1 = lines.get(1).split(",");
        assertTrue(cols1[0].contains("response1(Svc1)"));
        assertEquals("Svc1", cols1[1]);
        assertTrue(Arrays.asList(cols1).contains("10"));

        String[] cols2 = lines.get(2).split(",");
        assertTrue(cols2[0].contains("response2(Svc2)"));
        assertEquals("Svc2", cols2[1]);
        assertTrue(Arrays.asList(cols2).contains("5"));
    }
}
