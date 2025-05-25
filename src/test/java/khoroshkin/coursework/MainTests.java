package khoroshkin.coursework;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MainTests {

    @ParameterizedTest
    @MethodSource("validArgumentsProvider")
    void test_validInput(String[] args, int expectedNumThreads, int expectedTimeout, String[] expectedServices, String expectedFormat) {

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
    void test_invalidInput(String[] args, String expectedExceptionMessage) {
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
    void test_CreateApiServices() {
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

    @Test
    void test_analysis() {
        Analysis analysis = new Analysis();
        analysis.setError("error");
        analysis.setStatus("status");

        Analysis.AnalysisResult result = analysis.new AnalysisResult();
        result.setStats(result.new dataStats());
        result.getStats().setMalicious("1");
        result.getStats().setSuspicious("2");
        result.getStats().setUndetected("3");
        result.getStats().setUnsupported("0");
        analysis.setAttributes(result);

        Analysis.AnalysisData data = analysis.new AnalysisData();
        data.setId("id");
        analysis.setData(data);

        assertEquals("error", analysis.getError());
        assertEquals("status", analysis.getStatus());
        assertEquals("1", analysis.getAttributes().getStats().getMalicious());
        assertEquals("2", analysis.getAttributes().getStats().getSuspicious());
        assertEquals("3", analysis.getAttributes().getStats().getUndetected());
        assertEquals("0", analysis.getAttributes().getStats().getUnsupported());
        assertEquals("id", analysis.getData().getId());
    }

    @Test
    void test_spotifyInfo() {
        SpotifyInfo info = new SpotifyInfo();
        info.setId("id");
        info.setAccess_token("token");
        info.setName("name");
        info.setGenres(new String[]{"pop"});
        info.setSpotify("spotify");
        info.setType("type");
        info.setPopularity("popularity");
        info.setHref("href");
        SpotifyInfo.Follow followers = info.new Follow();
        followers.setTotal("100");
        info.setFollowers(followers);

        assertEquals("id", info.getId());
        assertEquals("token", info.getAccess_token());
        assertEquals("name", info.getName());
        assertEquals("type", info.getType());
        assertEquals("href", info.getHref());
        assertEquals("popularity", info.getPopularity());
        assertEquals("spotify", info.getSpotify());
        assertArrayEquals(new String[]{"pop"}, info.getGenres());
        assertEquals("100", info.getFollowers().getTotal());
    }

    @Test
    void test_transcript() {
        Transcript transcript = new Transcript();
        transcript.setId("id");
        transcript.setAudio_url("flexxmusixx.com");
        transcript.setLanguage_code("en_EN");
        transcript.setLanguage_model("en_Model");
        transcript.setStatus("completed");
        transcript.setText("text");

        assertEquals("id", transcript.getId());
        assertEquals("flexxmusixx.com", transcript.getAudio_url());
        assertEquals("en_EN", transcript.getLanguage_code());
        assertEquals("en_Model", transcript.getLanguage_model());
        assertEquals("completed", transcript.getStatus());
        assertEquals("text", transcript.getText());
    }
}