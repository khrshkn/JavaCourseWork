package khoroshkin.coursework;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class AssemblyAPI implements Runnable {
    private final String BASE_URL = "https://api.assemblyai.com/v2/transcript";
    private final String API_KEY = "7e4eb56d88244cca9ed7900cfe9fa6b2";
    private final String[] AUDIO_URL = {"https://drive.usercontent.google.com/u/0/uc?id=19TH3BKpdgIE2_ZMhx9rDRLDzfLLjhb4b&export=download",
                                        "https://drive.usercontent.google.com/u/0/uc?id=1Qs06m40Qld8Gkp9505tE2L1X7qs7g7RW&export=download",
                                        "https://assembly.ai/wildfires.mp3"};
    private DataWriter dataWriter;
    private HttpClient httpClient;
    private Gson gson;


    public AssemblyAPI(DataWriter dataWriter) {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new GsonBuilder().disableHtmlEscaping().create();
        this.dataWriter = dataWriter;
    }
    @Override
    public void run() {
        try {
            Transcript transcript = createTranscriptRequest();
            String result = getTranscription(transcript.getId());
            dataWriter.saveData(result, this.getClass().getSimpleName());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            // Logger
        }
    }

    private Transcript createTranscriptRequest() throws URISyntaxException, IOException, InterruptedException {
        Transcript transcript = new Transcript();
        transcript.setAudio_url(AUDIO_URL[new Random().nextInt(AUDIO_URL.length)]);

        String jsonRequest = gson.toJson(transcript);
        HttpRequest postRequest = buildPostRequest(jsonRequest);

        HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(postResponse.body(), Transcript.class);
    }

    private HttpRequest buildPostRequest(String jsonBody) throws URISyntaxException {
        return HttpRequest.newBuilder()
                .uri(new URI(BASE_URL))
                .header("Authorization", API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
    }

    private String getTranscription(String transcriptId)
            throws URISyntaxException, IOException, InterruptedException {
        HttpRequest getRequest = buildGetRequest(transcriptId);

        while (true) {
            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
            Transcript transcript = gson.fromJson(getResponse.body(), Transcript.class);

            if (transcript.getStatus().equals("completed") || transcript.getStatus().equals("error")) {
                return gson.toJson(transcript);
            }
            TimeUnit.SECONDS.sleep(2);
        }
    }

    private HttpRequest buildGetRequest(String transcriptId) throws URISyntaxException {
        return HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + "/" + transcriptId))
                .header("Authorization", API_KEY)
                .build();
    }
}