package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTVerifyMailCodeResponse {
    /**
     * Logger for this class.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecSignIDRESTVerifyMailCodeResponse.class);

    /**
     * The valid state.
     */
    private final boolean valid;

    /**
     * The exists state.
     */
    private final boolean exists;

    /**
     * Creates a response from JSON.
     * @param json the json of this response
     * @return the response
     * @throws JSONException thrown when a JSON error occurred
     * @throws SecSignIDRESTException thrown when a JSON key was not found
     */
    public static SecSignIDRESTVerifyMailCodeResponse fromJson(String json) throws JSONException, SecSignIDRESTException {
        JSONObject jsonObject = new JSONObject(json);
        if(!jsonObject.has("valid")) {
            logger.debug("Key 'valid' not found");
            throw new SecSignIDRESTException("Key 'valid' not found");
        }

        if(!jsonObject.has("exists")) {
            logger.debug("Key 'exists' not found");
            throw new SecSignIDRESTException("Key 'exists' not found");
        }

        boolean valid = jsonObject.getBoolean("valid");
        boolean exists = jsonObject.getBoolean("exists");
        return new SecSignIDRESTVerifyMailCodeResponse(valid, exists);
    }

    /**
     * Constructor for the response.
     * @param valid the valid state
     * @param exists the exists state
     */
    private SecSignIDRESTVerifyMailCodeResponse(boolean valid, boolean exists) {
        this.valid = valid;
        this.exists = exists;
    }

    /**
     * Get the valid state.
     * @return the valid state
     */
    public boolean getValid() {
        return valid;
    }

    /**
     * Get the exists state.
     * @return the exists state
     */
    public boolean getExists() {
        return exists;
    }
}
