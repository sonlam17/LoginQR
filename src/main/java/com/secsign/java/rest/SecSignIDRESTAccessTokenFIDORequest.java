package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTAccessTokenFIDORequest extends SecSignIDRESTAccessTokenRequest {
    /**
     * The credential ID.
     */
    private final String credentialId;

    /**
     * The client data JSON.
     */
    private final String clientDataJson;

    /**
     * The authenticator data.
     */
    private final String authenticatorData;

    /**
     * The signature.
     */
    private final String signature;

    /**
     * The user handle.
     */
    private final String userHandle;

    /**
     * Constructor for the request.
     * @param tokenId the token ID
     * @param credentialId the credential ID
     * @param clientDataJson the client data JSON
     * @param authenticatorData the authenticator data
     * @param signature the signature
     * @param userHandle the user handle
     */
    public SecSignIDRESTAccessTokenFIDORequest(String tokenId, String credentialId, String clientDataJson,
                                               String authenticatorData, String signature, String userHandle) {
        super(tokenId);
        this.credentialId = credentialId;
        this.clientDataJson = clientDataJson;
        this.authenticatorData = authenticatorData;
        this.signature = signature;
        this.userHandle = userHandle;
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject object = super.toJSONObject();
        object.put("credentialId", credentialId);
        object.put("clientDataJSON", clientDataJson);
        object.put("authenticatorData", authenticatorData);
        object.put("signature", signature);
        object.put("userHandle", userHandle);

        return object;
    }
}
