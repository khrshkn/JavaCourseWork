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

class SpotifyAPITest {

    private HttpClient mockHttpClient;
    private HttpResponse<String> mockTokenResponse;
    private HttpResponse<String> mockArtistResponse;
    private DataWriter mockDataWriter;
    private Gson gson;
    private SpotifyAPI spotifyAPI;

    @BeforeEach
    void setUp() {
        // Создаём моки
        mockHttpClient = mock(HttpClient.class);
        mockTokenResponse = mock(HttpResponse.class);
        mockArtistResponse = mock(HttpResponse.class);
        mockDataWriter = mock(DataWriter.class);
        gson = new GsonBuilder().disableHtmlEscaping().create();

        // Инжектим моки в тестируемый объект
        spotifyAPI = new SpotifyAPI(mockDataWriter);
    }

    @Test
    void testRun_successfulExecution_savesTokenAndArtistData() throws IOException, InterruptedException, URISyntaxException {
        // Подготовка фиктивных JSON-ответов
        String tokenJson  = "{\"access_token\":\"mock_token\"}";
        String artistJson = "{\"access_token\":\"artist_data\"}";

        // Настраиваем тела ответов
        when(mockTokenResponse.body()).thenReturn(tokenJson);
        when(mockArtistResponse.body()).thenReturn(artistJson);

        // Первый вызов send → токен, второй → данные артиста
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockTokenResponse)
                .thenReturn(mockArtistResponse);

        // Выполняем
        spotifyAPI.run();

        // Проверяем, что saveData вызвано дважды
        verify(mockDataWriter, times(2)).saveData(anyString(), eq("SpotifyAPI"));

        // Захватываем аргументы и проверяем их содержимое
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockDataWriter, times(2)).saveData(captor.capture(), eq("SpotifyAPI"));

        assertEquals(tokenJson,  captor.getAllValues().get(0));
        assertEquals(artistJson, captor.getAllValues().get(1));
    }

    @Test
    void testRun_whenHttpClientThrows_doesNotPropagateAndSavesAtMostOnce() throws IOException, InterruptedException, URISyntaxException {
        // Настраиваем send так, чтобы он выбрасывал IOException
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Network error"));

        // Выполняем — исключение ловится внутри run()
        spotifyAPI.run();

        // В логике requestAccessToken() saveData вызывается ДО броска
        // поэтому допускаем максимум один вызов
        verify(mockDataWriter, atMost(1)).saveData(anyString(), eq("SpotifyAPI"));
    }
}
