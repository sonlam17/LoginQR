package com.secsign.rest;

import org.json.JSONException;
import org.json.JSONObject;
import org.keycloak.authentication.AuthenticationFlowContext;

public class CreateAuthSessionResponse {
    /**
     * Logger for this class.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CreateAuthSessionResponse.class);

    /**
     * Response when the ID is frozen.
     */
    public static final CreateAuthSessionResponse FROZEN = new CreateAuthSessionResponse( null, null, null, true);



    /**
     * The auth session icon data (Base64 encoded image).
     */
    private final String authSessionIconData;
    private final String qrId;

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
     * @throws Exception thrown when a JSON key was not found
     */
    public static CreateAuthSessionResponse fromJson(String json, AuthenticationFlowContext context ) throws JSONException, Exception {
        JSONObject rootObject = new JSONObject(json);


        JSONObject data = rootObject.optJSONObject("data", null);
        String authSessionIconData = data.optString("qrContent", null);
        String qrId = data.optString("qrId", null);
        String secSignId = rootObject.optString("message", null);

        context.getAuthenticationSession().setUserSessionNote("qr.login.id", qrId);
        context.getAuthenticationSession().setUserSessionNote("qr.login.image", authSessionIconData);
        return new CreateAuthSessionResponse( authSessionIconData, qrId, secSignId, false);
    }

    /**
     * Constructor for the response.
     *
     * @param authSessionIconData the auth session icon data (Base64 encoded image)
     * @param qrId
     * @param secSignId           the SecSign ID
     * @param frozen              the frozen state of the SecSign ID
     */
    private CreateAuthSessionResponse( String authSessionIconData, String qrId, String secSignId, boolean frozen) {
        this.authSessionIconData = authSessionIconData;
        this.qrId = qrId;
        this.secSignId = secSignId;
        this.frozen = frozen;
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
    public String getQrId() {
        return qrId;
    }

    /**
     * Get the state of the ID, whether the SecSign ID is frozen or not.
     * @return the frozen state of the SecSign ID
     */
    public boolean getFrozen() {
        return frozen;
    }
}
