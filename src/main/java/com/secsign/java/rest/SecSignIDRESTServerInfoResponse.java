package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTServerInfoResponse {
    /**
     * Logger for this class.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecSignIDRESTServerInfoResponse.class);

    /**
     * The {@link SecSignIDServerVersion} returned by the response.
     */
    private final SecSignIDServerVersion version;

    /**
     * Creates a response from JSON.
     * @param json the json of this response
     * @return the response
     * @throws JSONException thrown when a JSON error occurred
     * @throws SecSignIDRESTException thrown when a JSON key was not found
     */
    public static SecSignIDRESTServerInfoResponse fromJson(String json) throws JSONException, SecSignIDRESTException {
        JSONObject rootObject = new JSONObject(json);
        int majorVersion = rootObject.optInt("majorVersion", -1);
        if (majorVersion == -1) {
            logger.debug("Key 'majorVersion' not found");
            throw new SecSignIDRESTException("Key 'majorVersion' not found");
        }

        int minorVersion = rootObject.optInt("minorVersion", -1);
        if (minorVersion == -1) {
            logger.debug("Key 'minorVersion' not found");
            throw new SecSignIDRESTException("Key 'minorVersion' not found");
        }

        int patchVersion = rootObject.optInt("patchVersion", -1);
        if (patchVersion == -1) {
            logger.debug("Key 'patchVersion' not found");
            throw new SecSignIDRESTException("Key 'patchVersion' not found");
        }

        SecSignIDServerVersion version = new SecSignIDServerVersion(majorVersion, minorVersion, patchVersion);
        return new SecSignIDRESTServerInfoResponse(version);
    }

    /**
     * Constructor for the response.
     * @param version the {@link SecSignIDServerVersion}
     */
    public SecSignIDRESTServerInfoResponse(SecSignIDServerVersion version) {
        this.version = version;
    }

    /**
     * Get the {@link SecSignIDServerVersion}.
     * @return the {@link SecSignIDServerVersion}
     */
    public SecSignIDServerVersion getVersion() {
        return version;
    }
}
