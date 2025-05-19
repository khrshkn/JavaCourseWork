package khoroshkin.coursework;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SpotifyAPI implements Runnable {
    private final String[] URLS = {"dsada"};
    private final String clientId = "62dd0a807d294e64b422e5c690465009";
    private final String clientSecret = "96c66b6d1c8e4a27b6ce1329aeba82f2";
    private HttpClient httpClient;
    private Gson gson;
    DataWriter dataWriter;

    public SpotifyAPI(DataWriter dataWriter) {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new GsonBuilder().disableHtmlEscaping().create();
        this.dataWriter = dataWriter;
    }

    @Override
    public void run() {
        try {
            SpotifyInfo info = requestAccessToken();
            SpotifyInfo artistData = requestArtistData(info.getAccess_token());
            String result = gson.toJson(artistData);
            dataWriter.saveData(result, this.getClass().getSimpleName());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            // Logger
        }
    }

    private SpotifyInfo requestAccessToken() throws URISyntaxException, IOException, InterruptedException {
        String formData = "grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret;
        HttpRequest postRequest = buildPostRequest(formData);
        HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(postResponse.body(), SpotifyInfo.class);
    }

    private HttpRequest buildPostRequest(String formData) throws URISyntaxException {
        return HttpRequest.newBuilder()
                .uri(new URI("https://accounts.spotify.com/api/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();
    }

    private SpotifyInfo requestArtistData(String token) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest getRequest = buildGetRequest(token);
        HttpResponse<String> getResponce = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(getResponce.body(), SpotifyInfo.class);
    }

    private HttpRequest buildGetRequest(String token) throws URISyntaxException {
        return HttpRequest.newBuilder()
                .uri(new URI("https://api.spotify.com/v1/artists/0uj6QiPsPfK8ywLC7uwBE1?si=PurRJ_qLTlSAY5cGAhwO-Q"))
                .header("Authorization", "Bearer " + token)
                .build();
    }
}