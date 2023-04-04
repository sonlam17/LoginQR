package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTExistResponse {
    /**
     * Logger for this class.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecSignIDRESTExistResponse.class);

    /**
     * The exist state of the SecSign ID.
     */
    private final boolean exist;

    /**
     * Creates a response from JSON.
     * @param json the json of this response
     * @return the response
     * @throws JSONException thrown when a JSON error occurred
     * @throws SecSignIDRESTException thrown when a JSON key was not found
     */
    public static SecSignIDRESTExistResponse fromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        if(!jsonObject.has("exist")) {
            logger.debug("Key 'exist' not found");
            return new SecSignIDRESTExistResponse(false);
        }

        Object existsValue = jsonObject.get("exist");
        boolean exist;
        if(existsValue instanceof Boolean) {
            exist = ((Boolean)existsValue).booleanValue();
        }
        else {
            exist = Boolean.parseBoolean(String.valueOf(existsValue));
        }

        return new SecSignIDRESTExistResponse(exist);
    }

    /**
     * Constructor for the response.
     * @param exist the exist state of the SecSign ID
     */
    private SecSignIDRESTExistResponse(boolean exist) {
        this.exist = exist;
    }

    /**
     * Get the exist state of the SecSign ID.
     * @return the exist state of the SecSign ID
     */
    public boolean getExist() {
        return exist;
    }
}
