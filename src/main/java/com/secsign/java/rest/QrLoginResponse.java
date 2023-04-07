package com.secsign.java.rest;

public class QrLoginResponse {

    public String state;
    public String userId;

    public QrLoginResponse(String state, String userId) {
        this.state = state;
        this.userId = userId;
    }
}
