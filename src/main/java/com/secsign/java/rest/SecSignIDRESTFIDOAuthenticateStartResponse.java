package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTFIDOAuthenticateStartResponse {
    /**
     * Logger for this class.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecSignIDRESTFIDOAuthenticateStartResponse.class);

    /**
     * The request options.
     */
    private final String requestOptions;

    /**
     * Creates a response from JSON.
     * @param json the json of this response
     * @return the response
     * @throws JSONException thrown when a JSON error occurred
     * @throws SecSignIDRESTException thrown when a JSON key was not found
     */
    public static SecSignIDRESTFIDOAuthenticateStartResponse fromJson(String json) throws JSONException, SecSignIDRESTException {
        JSONObject rootObject = new JSONObject(json);
        JSONObject requestOptions = rootObject.optJSONObject("requestOptions");
        if (requestOptions == null) {
            logger.debug("Key 'requestOptions' not found");
            throw new SecSignIDRESTException("Key 'requestOptions' not found");
        }

        return new SecSignIDRESTFIDOAuthenticateStartResponse(requestOptions.toString());
    }

    /**
     * Constructor for the response.
     * @param requestOptions the request options
     */
    public SecSignIDRESTFIDOAuthenticateStartResponse(String requestOptions) {
        this.requestOptions = requestOptions;
    }

    /**
     * Get the request options.
     * @return the request options
     */
    public String getRequestOptions() {
        return requestOptions;
    }
}
