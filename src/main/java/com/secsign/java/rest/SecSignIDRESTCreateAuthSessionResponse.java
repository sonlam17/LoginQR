package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTCreateAuthSessionResponse {
    /**
     * Logger for this class.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecSignIDRESTCreateAuthSessionResponse.class);

    /**
     * Response when the ID is frozen.
     */
    public static final SecSignIDRESTCreateAuthSessionResponse FROZEN = new SecSignIDRESTCreateAuthSessionResponse(0, null, null, true);

    /**
     * The auth session ID.
     */
    private final long authSessionId;

    /**
     * The auth session icon data (Base64 encoded image).
     */
    private final String authSessionIconData;

    /**
     * The SecSign ID.
     */
    private final String secSignId;

    /**
     * The state of the ID. If the ID is frozen, no auth session can be created.
     */
    private final boolean frozen;

    /**
     * Creates a response from JSON.
     * @param json the json of this response
     * @return the response
     * @throws JSONException thrown when a JSON error occurred
     * @throws SecSignIDRESTException thrown when a JSON key was not found
     */
    public static SecSignIDRESTCreateAuthSessionResponse fromJson(String json) throws JSONException, SecSignIDRESTException {
        JSONObject rootObject = new JSONObject(json);
        long authSessionId = rootObject.optLong("authsessionid", -1);
        if (authSessionId == -1) {
            logger.debug("Key 'authsessionid' not found");
            throw new SecSignIDRESTException("Key 'authsessionid' not found");
        }

        String authSessionIconData = rootObject.optString("authsessionicondata", null);
        String secSignId = rootObject.optString("secsignid", null);
        if (secSignId == null) {
            logger.debug("Key 'secsignid' not found");
            throw new SecSignIDRESTException("Key 'secsignid' not found");
        }

        return new SecSignIDRESTCreateAuthSessionResponse(authSessionId, authSessionIconData, secSignId, false);
    }

    /**
     * Constructor for the response.
     * @param authSessionId the auth session ID
     * @param authSessionIconData the auth session icon data (Base64 encoded image)
     * @param secSignId the SecSign ID
     * @param frozen the frozen state of the SecSign ID
     */
    private SecSignIDRESTCreateAuthSessionResponse(long authSessionId, String authSessionIconData, String secSignId, boolean frozen) {
        this.authSessionId = authSessionId;
        this.authSessionIconData = authSessionIconData;
        this.secSignId = secSignId;
        this.frozen = frozen;
    }

    /**
     * Get the auth session id.
     * @return the auth session id
     */
    public long getAuthSessionId() {
        return authSessionId;
    }

    /**
     * Get the auth session icon data (Base64 encoded image).
     * @return the auth session icon data
     */
    public String getAuthSessionIconData() {
        return authSessionIconData;
    }

    /**
     * Get the SecSign ID.
     * @return the SecSign ID
     */
    public String getSecSignId() {
        return secSignId;
    }

    /**
     * Get the state of the ID, whether the SecSign ID is frozen or not.
     * @return the frozen state of the SecSign ID
     */
    public boolean getFrozen() {
        return frozen;
    }
}
