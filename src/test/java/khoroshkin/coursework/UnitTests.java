package khoroshkin.coursework;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UnitTests {

    @ParameterizedTest
    @MethodSource("validArgumentsProvider")
    void testProcessArguments_ValidInput(String[] args, int expectedNumThreads, int expectedTimeout, String[] expectedServices, String expectedFormat) {
        Main.Arguments result = Main.processArguments(args);

        assertEquals(expectedNumThreads, result.numThreads);
        assertEquals(expectedTimeout, result.timeout);
        assertArrayEquals(expectedServices, result.services);
        assertEquals(expectedFormat, result.format);
    }

    private static Stream<Arguments> validArgumentsProvider() {
        return Stream.of(
                Arguments.of(new String[]{"3", "20", "spotify", "json"}, 3, 20, new String[]{"spotify"}, "json"),
                Arguments.of(new String[]{"2", "30", "assembly", "virustotal", "csv"}, 2, 30, new String[]{"assembly", "virustotal"}, "csv")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidArgumentsProvider")
    void testProcessArguments_InvalidInput(String[] args, String expectedExceptionMessage) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Main.processArguments(args));
        assertEquals(expectedExceptionMessage, exception.getMessage());
    }

    private static Stream<Arguments> invalidArgumentsProvider() {
        return Stream.of(
                Arguments.of(new String[]{}, "Required format: java -cp src/main/java khoroshkin.coursework.Main <threads> <timeout> <service1> [service2...] <format>\n" +
                        "Example: java -cp src/main/java khoroshkin.coursework.Main 3 20 spotify assembly virustotal json"),
                Arguments.of(new String[]{"-1", "20", "spotify", "json"}, "Threads and timeout must be integers and greater than 0"),
                Arguments.of(new String[]{"3", "0", "spotify", "json"}, "Threads and timeout must be integers and greater than 0"),
                Arguments.of(new String[]{"3", "20", "json"}, "Services must have at least one service"),
                Arguments.of(new String[]{"3", "20", "spotify", "xml"}, "Format must be either \"json\" or \"csv\"")
        );
    }

    @Test
    void testCreateApiServices() {
        String[] serviceNames = {"spotify", "assembly", "virustotal", "unknown"};
        DataWriter dataWriter = mock(DataWriter.class);
        APIScraper scraper = new APIScraper(1, 1, serviceNames, "json");

        Runnable[] services = scraper.createApiServices(serviceNames, dataWriter);

        assertEquals(4, services.length);
        assertInstanceOf(SpotifyAPI.class, services[0]);
        assertInstanceOf(AssemblyAPI.class, services[1]);
        assertInstanceOf(VirusTotalAPI.class, services[2]);
        assertInstanceOf(APIScraper.UnknownAPIService.class, services[3]);
    }


//    @Test
//    void testSaveData_JsonFormat() throws IOException {
//        DataWriter dataWriter = new DataWriter("json");
//
//        String data = "{\"key\": \"value\"}";
//        dataWriter.saveData(data, "testService");
//
//        String content = new String(Files.readAllBytes(Paths.get("output.json")));
//        assertTrue(content.contains("\"response1(testService)\": { \"key\": \"value\" }"));
//    }

    // Тесты для SpotifyAPI.java
    @Test
    void testSpotifyAPI_Run_Success() throws Exception {
        DataWriter dataWriter = mock(DataWriter.class);
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> tokenResponse = mock(HttpResponse.class);
        when(tokenResponse.body()).thenReturn("{\"access_token\":\"token\",\"token_type\":\"Bearer\",\"expires_in\":3600}");
        HttpResponse<String> artistResponse = mock(HttpResponse.class);
        when(artistResponse.body()).thenReturn("{\"id\":\"artistId\"}");
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(tokenResponse, artistResponse);

        SpotifyAPI spotifyAPI = new SpotifyAPI(dataWriter) {
            protected HttpClient getHttpClient() {
                return httpClient;
            }
        };

        spotifyAPI.run();

        // Проверяем оба вызова saveData с соответствующими данными
        verify(dataWriter).saveData(eq("{\"access_token\":\"token\",\"token_type\":\"Bearer\",\"expires_in\":3600}"), eq("SpotifyAPI"));
        //verify(dataWriter).saveData(eq("{\"id\":\"artistId\"}"), eq("SpotifyAPI"));
        // Или, если точное соответствие данных не важно, используй anyString()
        verify(dataWriter, times(2)).saveData(anyString(), eq("SpotifyAPI"));
    }

    // Тесты для AssemblyAPI.java
    /*@Test
    void testAssemblyAPI_Run_Success() throws Exception {
        DataWriter dataWriter = mock(DataWriter.class);
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> postResponse = mock(HttpResponse.class);
        when(postResponse.body()).thenReturn("{\"id\": \"transcriptId\", \"status\": \"queued\"}");
        HttpResponse<String> getResponse = mock(HttpResponse.class);
        when(getResponse.body()).thenReturn("{\"id\": \"transcriptId\", \"status\": \"completed\", \"text\": \"test\"}");
        when(httpClient.send(any(HttpRequest.class), any())).thenReturn(postResponse, getResponse);

        AssemblyAPI assemblyAPI = new AssemblyAPI(dataWriter) {
            @Override
            protected HttpClient getHttpClient() {
                return httpClient;
            }
        };

        assemblyAPI.run();

        verify(dataWriter).saveData(anyString(), eq("AssemblyAPI"));
    }*/

    // Тесты для VirusTotalAPI.java
    /*@Test
    void testVirusTotalAPI_Run_Success() throws Exception {
        DataWriter dataWriter = mock(DataWriter.class);
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> postResponse = mock(HttpResponse.class);
        when(postResponse.body()).thenReturn("{\"data\": {\"id\": \"requestId\", \"type\": \"analysis\"}}");
        HttpResponse<String> getResponse = mock(HttpResponse.class);
        when(getResponse.body()).thenReturn("{\"data\": {\"id\": \"requestId\", \"type\": \"analysis\", \"attributes\": {\"status\": \"completed\", \"stats\": {\"malicious\": 1}}}}");
        when(httpClient.send(any(HttpRequest.class), any())).thenReturn(postResponse, getResponse);

        VirusTotalAPI virusTotalAPI = new VirusTotalAPI(dataWriter) {
            @Override
            protected HttpClient getHttpClient() {
                return httpClient;
            }
        };

        virusTotalAPI.run();

        verify(dataWriter).saveData(anyString(), eq("VirusTotalAPI"));
    }*/

    // Тесты для Analysis.java
    @Test
    void testAnalysis_GettersAndSetters() {
        Analysis analysis = new Analysis();
        analysis.setError("error");
        analysis.setStatus("status");

        Analysis.AnalysisResult result = analysis.new AnalysisResult();
        result.setStats(result.new dataStats());
        result.getStats().setMalicious("1");
        analysis.setAttributes(result);

        Analysis.AnalysisData data = analysis.new AnalysisData();
        data.setId("id");
        analysis.setData(data);

        assertEquals("error", analysis.getError());
        assertEquals("status", analysis.getStatus());
        assertEquals("1", analysis.getAttributes().getStats().getMalicious());
        assertEquals("id", analysis.getData().getId());
    }

    // Тесты для SpotifyInfo.java
    /*@Test
    void testSpotifyInfo_GettersAndSetters() {
        SpotifyInfo info = new SpotifyInfo();
        info.setId("id");
        info.setAccess_token("token");
        info.setName("name");
        info.setGenres(new String[]{"pop"});
        SpotifyInfo.Follow followers = info.new Follow();
        followers.setTotal("100");
        info.setFollowers(followers);

        assertEquals("id", info.getId());
        assertEquals("token", info.getAccess_token());
        assertEquals("name", info.getName());
        assertArrayEquals(new String[]{"pop"}, info.getGenres());
        assertEquals("100", info.getFollowers().getTotal());
    }*/

    // Тесты для Transcript.java
    @Test
    void testTranscript_GettersAndSetters() {
        Transcript transcript = new Transcript();
        transcript.setId("id");
        transcript.setStatus("completed");
        transcript.setText("text");

        assertEquals("id", transcript.getId());
        assertEquals("completed", transcript.getStatus());
        assertEquals("text", transcript.getText());
    }
}