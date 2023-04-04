package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTAccessTokenRequest {
    /**
     * The token ID.
     */
    private final String tokenId;

    /**
     * Constructor for a access token request.
     * @param tokenId the token ID
     */
    public SecSignIDRESTAccessTokenRequest(String tokenId) {
        this.tokenId = tokenId;
    }

    /**
     * Convert the request to a {@link JSONObject}.
     * @return the request in a {@link JSONObject}
     * @throws JSONException thrown if a JSON error occurred
     */
    public JSONObject toJSONObject() throws JSONException {
        return new JSONObject();
    }

    /**
     * Get the token ID.
     * @return the token ID
     */
    public String getTokenId() {
        return tokenId;
    }
}
