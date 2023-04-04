package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTAccessTokenTOTPRequest extends SecSignIDRESTAccessTokenRequest {
    /**
     * The TOTP.
     */
    private final String totp;

    /**
     * Constructor for the request.
     * @param tokenId the token ID
     * @param totp the TOTP
     */
    public SecSignIDRESTAccessTokenTOTPRequest(String tokenId, String totp) {
        super(tokenId);
        this.totp = totp;
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject object = super.toJSONObject();
        object.put("totp", totp);

        return object;
    }
}
