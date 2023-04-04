package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTCheckAuthSessionStateResponse {
    /**
     * Logger for this class.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecSignIDRESTCheckAuthSessionStateResponse.class);

    /**
     * Raw value of the auth session state.
     */
    private final int authSessionStateValue;

    /**
     * The state of the auth session.
     */
    private final State authSessionState;

    /**
     * Creates a response from JSON.
     * @param json the json of this response
     * @return the response
     * @throws JSONException thrown when a JSON error occurred
     * @throws SecSignIDRESTException thrown when a JSON key was not found
     */
    public static SecSignIDRESTCheckAuthSessionStateResponse fromJson(String json) throws JSONException, SecSignIDRESTException {
        JSONObject rootObject = new JSONObject(json);
        int authSessionState = rootObject.optInt("authsessionstate", -1);
        if (authSessionState == -1) {
            logger.debug("Key 'authsessionstate' not found");
            throw new SecSignIDRESTException("Key 'authsessionstate' not found");
        }

        return new SecSignIDRESTCheckAuthSessionStateResponse(authSessionState, getStateFromValue(authSessionState));
    }

    /**
     * Constructor for the response.
     * @param authSessionStateValue the raw value of the auth session state
     * @param authSessionState the state of the auth session
     */
    private SecSignIDRESTCheckAuthSessionStateResponse(int authSessionStateValue, State authSessionState) {
        this.authSessionStateValue = authSessionStateValue;
        this.authSessionState = authSessionState;
    }

    /**
     * Get the raw auth session state value.
     * @return the raw auth session state value
     */
    public int getAuthSessionStateValue() {
        return authSessionStateValue;
    }

    /**
     * Get the auth session state.
     * @return the auth session state
     */
    public State getAuthSessionState() {
        return authSessionState;
    }

    /**
     * Converts the raw value to a state.
     * @param value the raw value of the state
     * @return the state
     */
    private static State getStateFromValue(int value) {
        switch (value) {
            case 1:
                return State.PENDING;
            case 2:
                return State.EXPIRED;
            case 3:
                return State.AUTHENTICATED;
            case 4:
                return State.DENIED;
            case 5:
                return State.SUSPENDED;
            case 6:
                return State.CANCELED;
            case 7:
                return State.FETCHED;
            case 8:
                return State.INVALID;
            default:
                return State.NO_STATE;
        }
    }

    public enum State {
        /**
         * Value 0
         * Used when the session state is undefined.
         */
        NO_STATE,

        /**
         * Value 1
         * The session is still pending for authentication.
         */
        PENDING,

        /**
         * Value 2
         * The authentication timeout has been exceeded.
         */
        EXPIRED,

        /**
         * Value 3
         * The user was successfully authenticated and accepted the session.
         */
        AUTHENTICATED,

        /**
         * Value 4
         * The user has denied the session.
         */
        DENIED,

        /**
         * Value 5
         * The server suspended this session, because another authentication request
         * was received while this session was still pending.
         */
        SUSPENDED,

        /**
         * Value 6
         * The service has canceled this session.
         */
        CANCELED,

        /**
         * Value 7
         * The device has already fetched the session, but the session
         * hasn't been authenticated or denied yet.
         */
        FETCHED,

        /**
         * Value 8
         * This session has become invalid.
         */
        INVALID
    }
}
