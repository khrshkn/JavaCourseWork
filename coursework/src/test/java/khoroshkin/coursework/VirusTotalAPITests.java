package khoroshkin.coursework;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VirusTotalAPITests {

    private HttpClient mockHttpClient;
    private HttpResponse<String> mockPostResponse;
    private HttpResponse<String> mockGetResponse;
    private DataWriter mockDataWriter;
    private Gson gson;
    private VirusTotalAPI api;

    @BeforeEach
    void config() {
        mockHttpClient = mock(HttpClient.class);
        mockPostResponse = mock(HttpResponse.class);
        mockGetResponse = mock(HttpResponse.class);
        mockDataWriter = mock(DataWriter.class);
        gson = new GsonBuilder().disableHtmlEscaping().create();

        api = new VirusTotalAPI(mockHttpClient, gson, mockDataWriter) {
            @Override
            protected String buildPostRequest() throws URISyntaxException, IOException, InterruptedException {
                return "req-123";
            }
        };
    }

    @Test
    void test_successful() throws Exception {
        JsonObject full = new JsonObject();
        JsonObject data = new JsonObject();
        JsonObject attrs = new JsonObject();
        JsonObject stats = new JsonObject();
        data.addProperty("id", "req-123");
        data.addProperty("type", "analysis");
        attrs.addProperty("status", "completed");
        stats.addProperty("malicious", 5);
        stats.addProperty("suspicious", 2);
        stats.addProperty("undetected", 90);
        stats.addProperty("harmless", 3);
        stats.addProperty("timeout", 0);
        stats.addProperty("confirmed-timeout", 0);
        stats.addProperty("failure", 0);
        stats.addProperty("type-unsupported", 0);
        attrs.add("stats", stats);
        data.add("attributes", attrs);
        full.add("data", data);
        String getJson = gson.toJson(full);

        when(mockGetResponse.body()).thenReturn(getJson);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockGetResponse);

        api.run();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockDataWriter, times(1))
                .saveData(captor.capture(), anyString());

        JsonObject out = gson.fromJson(captor.getValue(), JsonObject.class);
        assertEquals("req-123",      out.get("id").getAsString());
        assertEquals("analysis",     out.get("type").getAsString());
        assertEquals("completed",    out.get("status").getAsString());

        JsonObject s = out.getAsJsonObject("stats");
        assertEquals(5,  s.get("malicious").getAsInt());
        assertEquals(2,  s.get("suspicious").getAsInt());
        assertEquals(90, s.get("undetected").getAsInt());
        assertEquals(3,  s.get("harmless").getAsInt());
        assertEquals(0,  s.get("timeout").getAsInt());
        assertEquals(0,  s.get("confirmed-timeout").getAsInt());
        assertEquals(0,  s.get("failure").getAsInt());
        assertEquals(0,  s.get("type-unsupported").getAsInt());
    }

    @Test
    void test_throws() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Network down"));

        assertDoesNotThrow(() -> api.run());
        verify(mockDataWriter, never()).saveData(anyString(), anyString());
    }
}
