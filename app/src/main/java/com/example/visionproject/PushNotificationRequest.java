package com.example.visionproject;

public class PushNotificationRequest {

    private String targetToken;
    private String title;
    private String body;
    private String id;
    private String isEnd;

    public PushNotificationRequest() {
    }

    public PushNotificationRequest(String targetToken, String title, String body, String id, String isEnd) {
        this.targetToken = targetToken;
        this.title = title;
        this.body = body;
        this.id = id;
        this.isEnd = isEnd;
    }

    public void setTargetToken(String targetToken) {
        this.targetToken = targetToken;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIsEnd(String isEnd) {
        this.isEnd = isEnd;
    }

    public String getIsEnd() {
        return isEnd;
    }

    public String getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public String getTitle() {
        return title;
    }

    public String getTargetToken() {
        return targetToken;
    }
}