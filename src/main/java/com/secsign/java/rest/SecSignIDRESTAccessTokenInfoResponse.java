package com.secsign.java.rest;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTAccessTokenInfoResponse {
    /**
     * Logger for this class.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecSignIDRESTAccessTokenInfoResponse.class);

    /**
     * A default response when no access token is needed.
     */
    public static final SecSignIDRESTAccessTokenInfoResponse NO_ACCESS_TOKEN_RESPONSE = new SecSignIDRESTAccessTokenInfoResponse(
            new SecSignIDRESTAuthenticationMethod[0], true);

    /**
     * The {@link SecSignIDRESTAuthenticationMethod} array which are activated.
     */
    private final SecSignIDRESTAuthenticationMethod[] authenticationMethods;

    /**
     * The state of access allowed without an token.
     */
    private final boolean accessAllowedWithoutToken;

    /**
     * Creates a response from JSON.
     * @param json the json of this response
     * @return the response
     * @throws JSONException thrown when a JSON error occurred
     * @throws SecSignIDRESTException thrown when a JSON key was not found
     */
    public static SecSignIDRESTAccessTokenInfoResponse fromJson(String json) throws JSONException, SecSignIDRESTException {
        JSONObject rootObject = new JSONObject(json);
        JSONArray authenticationMethods = rootObject.optJSONArray("authmethods");
        if (authenticationMethods == null) {
            logger.debug("Key 'authmethods' not found");
            throw new SecSignIDRESTException("Key 'authmethods' not found");
        }

        List<SecSignIDRESTAuthenticationMethod> authenticationMethodList = new ArrayList<SecSignIDRESTAuthenticationMethod>();
        for(int i = 0; i < authenticationMethods.length(); i++) {
            String authenticationMethod = authenticationMethods.getString(i);
            SecSignIDRESTAuthenticationMethod method = SecSignIDRESTAuthenticationMethod.fromName(authenticationMethod);
            
            if (method == SecSignIDRESTAuthenticationMethod.UNKNOWN) {
                String message = "Authentication method with name '" + authenticationMethod + "' is unknown!";
                logger.debug(message);
                throw new SecSignIDRESTException(message);
            }

            authenticationMethodList.add(method);
        }

        if (!rootObject.has("accessAllowedWithoutToken")) {
            logger.debug("Key 'accessAllowedWithoutToken' not found");
            throw new SecSignIDRESTException("Key 'accessAllowedWithoutToken' not found");
        }

        boolean accessAllowedWithoutToken = rootObject.getBoolean("accessAllowedWithoutToken");

        return new SecSignIDRESTAccessTokenInfoResponse(authenticationMethodList.toArray(new SecSignIDRESTAuthenticationMethod[0]),
                accessAllowedWithoutToken);
    }

    /**
     * Constructor for the response.
     * @param authenticationMethods the {@link SecSignIDRESTAuthenticationMethod} array which are activated
     * @param accessAllowedWithoutToken the state of access allowed without an token
     */
    private SecSignIDRESTAccessTokenInfoResponse(SecSignIDRESTAuthenticationMethod[] authenticationMethods,
                                                 boolean accessAllowedWithoutToken) {
        this.authenticationMethods = authenticationMethods;
        this.accessAllowedWithoutToken = accessAllowedWithoutToken;
    }

    /**
     * Get the {@link SecSignIDRESTAuthenticationMethod} array which are activated.
     * @return the {@link SecSignIDRESTAuthenticationMethod} array which are activated
     */
    public SecSignIDRESTAuthenticationMethod[] getAuthenticationMethods() {
        return authenticationMethods;
    }

    /**
     * Get the state of access allowed without an token.
     * @return the state of access allowed without an token
     */
    public boolean isAccessAllowedWithoutToken() {
        return accessAllowedWithoutToken;
    }
}
