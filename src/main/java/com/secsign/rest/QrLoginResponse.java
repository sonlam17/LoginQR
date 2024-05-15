package com.secsign.rest;

public class QrLoginResponse {

    public Boolean state;
    public String userId;

    public QrLoginResponse(Boolean state, String userId) {
        this.state = state;
        this.userId = userId;
    }

    public Boolean getState() {
        return state;
    }


    public String getUserId() {
        return userId;
    }
}
