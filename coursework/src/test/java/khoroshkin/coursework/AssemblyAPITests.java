package khoroshkin.coursework;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssemblyAPITests {

    private HttpClient mockHttpClient;
    private HttpResponse<String> mockPostResponse;
    private HttpResponse<String> mockGetResponse;
    private DataWriter mockDataWriter;
    private Gson gson;
    private AssemblyAPI assemblyAPI;

    @BeforeEach
    void config() {
        mockHttpClient = mock(HttpClient.class);
        mockPostResponse = mock(HttpResponse.class);
        mockGetResponse = mock(HttpResponse.class);
        mockDataWriter = mock(DataWriter.class);
        gson = new GsonBuilder().disableHtmlEscaping().create();

        assemblyAPI = new AssemblyAPI(mockHttpClient, gson, mockDataWriter);
    }

    @Test
    void test_successful() throws IOException, InterruptedException {
        Transcript created = new Transcript();
        created.setId("id1337");

        Transcript completed = new Transcript();
        completed.setId("id1337");
        completed.setStatus("completed");
        completed.setText("Hello world");

        String postJson = gson.toJson(created);
        String getJson  = gson.toJson(completed);

        when(mockPostResponse.body()).thenReturn(postJson);
        when(mockGetResponse.body()).thenReturn(getJson);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockPostResponse)
                .thenReturn(mockGetResponse);

        assemblyAPI.run();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockDataWriter, times(2))
                .saveData(captor.capture(), eq("AssemblyAPI"));

        Transcript saved = gson.fromJson(captor.getValue(), Transcript.class);
        assertEquals("id1337", saved.getId());
        assertEquals("completed", saved.getStatus());
        assertEquals("Hello world", saved.getText());
    }

    @Test
    void test_throws() throws IOException, InterruptedException {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("network"));

        assertDoesNotThrow(() -> assemblyAPI.run());

        verify(mockDataWriter, never()).saveData(anyString(), anyString());
    }
}
