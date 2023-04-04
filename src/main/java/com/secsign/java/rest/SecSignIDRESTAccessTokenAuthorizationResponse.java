package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTAccessTokenAuthorizationResponse {
    /**
     * Logger for this class.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecSignIDRESTAccessTokenAuthorizationResponse.class);

    /**
     * The Token ID.
     */
    private final String tokenId;

    /**
     * The Auth Session ID.
     * Can be null.
     */
    private final long authSessionId;

    /**
     * The FIDO informations.
     * Can be null.
     */
    private final String fido;

    /**
     * Creates a response from JSON.
     * @param json the json of this response
     * @return the response
     * @throws JSONException thrown when a JSON error occurred
     * @throws SecSignIDRESTException thrown when a JSON key was not found
     */
    public static SecSignIDRESTAccessTokenAuthorizationResponse fromJson(String json) throws JSONException, SecSignIDRESTException {
        JSONObject rootObject = new JSONObject(json);
        String tokenId = rootObject.optString("tokenid", null);
        if (tokenId == null) {
            logger.debug("Key 'tokenid' not found");
            throw new SecSignIDRESTException("Key 'tokenid' not found");
        }

        long authSessionId = rootObject.optLong("authsessionid", -1);
        String fido = rootObject.optString("fido", null);

        return new SecSignIDRESTAccessTokenAuthorizationResponse(tokenId, authSessionId, fido);
    }

    /**
     * Constructor for the response.
     * @param tokenId the token ID.
     * @param authSessionId the auth session ID
     * @param fido the FIDO informations
     */
    private SecSignIDRESTAccessTokenAuthorizationResponse(String tokenId, long authSessionId, String fido) {
        this.tokenId = tokenId;
        this.authSessionId = authSessionId;
        this.fido = fido;
    }

    /**
     * Get the token ID.
     * @return the token ID
     */
    public String getTokenId() {
        return tokenId;
    }

    /**
     * Get the auth session ID.
     * @return the auth session ID
     */
    public long getAuthSessionId() {
        return authSessionId;
    }

    /**
     * Get the FIDO informations.
     * @return the FIDO informations
     */
    public String getFido() {
        return fido;
    }
}
