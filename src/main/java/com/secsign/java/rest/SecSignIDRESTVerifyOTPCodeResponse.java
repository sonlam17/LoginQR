package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTVerifyOTPCodeResponse {
    /**
     * Logger for this class.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecSignIDRESTVerifyOTPCodeResponse.class);

    /**
     * The valid state.
     */
    private final boolean valid;

    /**
     * Creates a response from JSON.
     * @param json the json of this response
     * @return the response
     * @throws JSONException thrown when a JSON error occurred
     * @throws SecSignIDRESTException thrown when a JSON key was not found
     */
    public static SecSignIDRESTVerifyOTPCodeResponse fromJson(String json) throws JSONException, SecSignIDRESTException {
        JSONObject jsonObject = new JSONObject(json);
        if(!jsonObject.has("valid")) {
            logger.debug("Key 'valid' not found");
            throw new SecSignIDRESTException("Key 'valid' not found");
        }

        boolean valid = jsonObject.getBoolean("valid");
        return new SecSignIDRESTVerifyOTPCodeResponse(valid);
    }

    /**
     * Constructor for the response.
     * @param valid the valid state
     */
    private SecSignIDRESTVerifyOTPCodeResponse(boolean valid) {
        this.valid = valid;
    }

    /**
     * Get the valid state.
     * @return the valid state
     */
    public boolean getValid() {
        return valid;
    }
}
