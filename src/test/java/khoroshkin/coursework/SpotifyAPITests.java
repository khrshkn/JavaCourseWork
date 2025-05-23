package khoroshkin.coursework;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SpotifyAPITests {

    private HttpClient mockHttpClient;
    private HttpResponse<String> mockTokenResponse;
    private HttpResponse<String> mockArtistResponse;
    private DataWriter mockDataWriter;
    private Gson gson;
    private SpotifyAPI spotifyAPI;

    @BeforeEach
    void config() {
        mockHttpClient = mock(HttpClient.class);
        mockTokenResponse = mock(HttpResponse.class);
        mockArtistResponse = mock(HttpResponse.class);
        mockDataWriter = mock(DataWriter.class);
        gson = new GsonBuilder().disableHtmlEscaping().create();

        spotifyAPI = new SpotifyAPI(mockHttpClient, gson, mockDataWriter);
    }

    @Test
    void test_successful() throws IOException, InterruptedException, URISyntaxException {
        String tokenJson  = "{\"access_token\":\"mock_token\"}";
        String artistJson = "{\"access_token\":\"artist_data\"}";

        when(mockTokenResponse.body()).thenReturn(tokenJson);
        when(mockArtistResponse.body()).thenReturn(artistJson);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockTokenResponse)
                .thenReturn(mockArtistResponse);

        spotifyAPI.run();

        verify(mockDataWriter, times(2)).saveData(anyString(), eq("SpotifyAPI"));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockDataWriter, times(2)).saveData(captor.capture(), eq("SpotifyAPI"));

        assertEquals(tokenJson,  captor.getAllValues().get(0));
        assertEquals(artistJson, captor.getAllValues().get(1));
    }

    @Test
    void test_throw() throws IOException, InterruptedException {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Network error"));

        spotifyAPI.run();

        verify(mockDataWriter, atMost(1)).saveData(anyString(), eq("SpotifyAPI"));
    }
}
