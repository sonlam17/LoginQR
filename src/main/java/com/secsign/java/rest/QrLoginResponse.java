package com.secsign.java.rest;

public class QrLoginResponse {

    public String state;
    public String userName;
    public String accessToken;

    public QrLoginResponse(String state, String userName, String accessToken) {
        this.state = state;
        this.userName = userName;
        this.accessToken = accessToken;
    }
}
