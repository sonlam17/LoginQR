package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTFIDORegisterStartResponse {
    /**
     * Logger for this class.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecSignIDRESTFIDORegisterStartResponse.class);

    /**
     * The creation options.
     */
    private final String creationOptions;

    /**
     * Creates a response from JSON.
     * @param json the json of this response
     * @return the response
     * @throws JSONException thrown when a JSON error occurred
     * @throws SecSignIDRESTException thrown when a JSON key was not found
     */
    public static SecSignIDRESTFIDORegisterStartResponse fromJson(String json) throws JSONException, SecSignIDRESTException {
        JSONObject rootObject = new JSONObject(json);
        JSONObject creationOptions = rootObject.optJSONObject("creationOptions");
        if (creationOptions == null) {
            logger.debug("Key 'creationOptions' not found");
            throw new SecSignIDRESTException("Key 'creationOptions' not found");
        }

        return new SecSignIDRESTFIDORegisterStartResponse(creationOptions.toString());
    }

    /**
     * Constructor for the response.
     * @param creationOptions the creation options
     */
    public SecSignIDRESTFIDORegisterStartResponse(String creationOptions) {
        this.creationOptions = creationOptions;
    }

    /**
     * Get the creation options.
     * @return the creation options
     */
    public String getCreationOptions() {
        return creationOptions;
    }
}
