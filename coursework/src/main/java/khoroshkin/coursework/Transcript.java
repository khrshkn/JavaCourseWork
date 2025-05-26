package khoroshkin.coursework;

public class Transcript {
    private String id;
    private String language_model;
    private String language_code;
    private String status;
    private String audio_url;
    private String text;

    public String getLanguage_model() {
        return language_model;
    }

    public void setLanguage_model(String language_model) {
        this.language_model = language_model;
    }

    public String getLanguage_code() {
        return language_code;
    }

    public void setLanguage_code(String language_code) {
        this.language_code = language_code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAudio_url(String audio_url) {
        this.audio_url = audio_url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAudio_url() {
        return audio_url;
    }

}
