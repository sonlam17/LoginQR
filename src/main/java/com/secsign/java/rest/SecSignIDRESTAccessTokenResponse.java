package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTAccessTokenResponse {
    /**
     * Logger for this class.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecSignIDRESTAccessTokenResponse.class);

    /**
     * The token.
     */
    private final String token;

    /**
     * The UNIX timestamp in milliseconds when the token expires.
     */
    private final long validUntil;

    /**
     * Creates a response from JSON.
     * @param json the json of this response
     * @return the response
     * @throws JSONException thrown when a JSON error occurred
     * @throws SecSignIDRESTException thrown when a JSON key was not found
     */
    public static SecSignIDRESTAccessTokenResponse fromJson(String json) throws JSONException, SecSignIDRESTException {
        JSONObject rootObject = new JSONObject(json);
        String token = rootObject.optString("token", null);
        if (token == null) {
            logger.debug("Key 'token' not found");
            throw new SecSignIDRESTException("Key 'token' not found");
        }

        long validUntil = rootObject.optLong("validuntil", -1);
        if (validUntil == -1) {
            logger.debug("Key 'validuntil' not found");
            throw new SecSignIDRESTException("Key 'validuntil' not found");
        }

        return new SecSignIDRESTAccessTokenResponse(token, validUntil);
    }

    /**
     * Constructor for the response.
     * @param token the token
     * @param validUntil the UNIX timestamp in milliseconds when the token expires.
     */
    private SecSignIDRESTAccessTokenResponse(String token, long validUntil) {
        this.token = token;
        this.validUntil = validUntil;
    }

    /**
     * Get the token.
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * Get the UNIX timestamp in milliseconds when the token expires.
     * @return the UNIX timestamp in milliseconds when the token expires
     */
    public long getValidUntil() {
        return validUntil;
    }
}
