package khoroshkin.coursework;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VirusTotalAPI implements Runnable {
    private static final Logger logger = LogManager.getLogger(VirusTotalAPI.class);
    private final String ENDPOINT_URL = "https://www.virustotal.com/api/v3/analyses";
    private final String API_KEY = System.getenv("VIRUSTOTAL_API_KEY");;
    private final String FILE_PATH = "trojan.txt";
    private final HttpClient httpClient;
    private final Gson gson;
    private final DataWriter dataWriter;

    public VirusTotalAPI(HttpClient httpClient, Gson gson, DataWriter dataWriter) {
        this.httpClient = httpClient;
        this.gson = gson;
        this.dataWriter = dataWriter;
    }

    public VirusTotalAPI(DataWriter dataWriter) {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new GsonBuilder().disableHtmlEscaping().create();
        this.dataWriter = dataWriter;
    }

    @Override
    public void run() {
        try {
            String requestId = buildPostRequest();
            String result = getFileAnalysis(requestId);
            dataWriter.saveData(result, this.getClass().getSimpleName());
            logger.info("Successfully fetched and saved data for VirusTotalAPI");
        } catch (URISyntaxException | IOException |  NullPointerException | InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Failed to fetch data from VirusTotal API", e);
        }
    }

    protected String buildPostRequest() throws URISyntaxException, IOException, InterruptedException, NullPointerException {
        String boundary = UUID.randomUUID().toString();
        String multipartBody = createMultipart(boundary);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.virustotal.com/api/v3/files"))
                .header("accept", "application/json")
                .header("x-apikey", API_KEY)
                .header("content-type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofString(multipartBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Analysis analysis = gson.fromJson(response.body(), Analysis.class);
        if (response.statusCode() != 200) {
            throw new IOException(response.body());
        }
        if (analysis.getError() != null) {
            TimeUnit.SECONDS.sleep(3);
            buildPostRequest();
        }
        return analysis.getData().getId();
    }

    protected String createMultipart(String boundary) throws IOException {
        Path filePath = Path.of(FILE_PATH);
        byte[] fileBytes = Files.readAllBytes(filePath);

        String multipartBody = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + filePath.getFileName() + "\"\r\n" +
                "Content-Type: text/plain\r\n\r\n" +
                new String(fileBytes) + "\r\n" +
                "--" + boundary + "--\r\n";
        return multipartBody;
    }

    private String getFileAnalysis(String requestId) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest getRequest = buildGetRequest(requestId);
        HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        if (getResponse.statusCode() != 200) {
            throw new IOException(getResponse.body());
        }
        JsonObject fullResponse = gson.fromJson(getResponse.body(), JsonObject.class);
        JsonObject data = fullResponse.getAsJsonObject("data");

        JsonObject simplifiedResponse = new JsonObject();
        simplifiedResponse.addProperty("id", data.get("id").getAsString());
        simplifiedResponse.addProperty("type", data.get("type").getAsString());
        simplifiedResponse.addProperty("status", data.getAsJsonObject("attributes").get("status").getAsString());

        JsonObject stats = new JsonObject();
        JsonObject fullStats = data.getAsJsonObject("attributes").getAsJsonObject("stats");
        stats.addProperty("malicious", fullStats.get("malicious").getAsInt());
        stats.addProperty("suspicious", fullStats.get("suspicious").getAsInt());
        stats.addProperty("undetected", fullStats.get("undetected").getAsInt());
        stats.addProperty("harmless", fullStats.get("harmless").getAsInt());
        stats.addProperty("timeout", fullStats.get("timeout").getAsInt());
        stats.addProperty("confirmed-timeout", fullStats.get("confirmed-timeout").getAsInt());
        stats.addProperty("failure", fullStats.get("failure").getAsInt());
        stats.addProperty("type-unsupported", fullStats.get("type-unsupported").getAsInt());
        simplifiedResponse.add("stats", stats);

        return gson.toJson(simplifiedResponse);
    }

    private HttpRequest buildGetRequest(String id) {
        return HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT_URL + "/" + id))
                .header("accept", "application/json")
                .header("x-apikey", API_KEY)
                .build();
    }
}
