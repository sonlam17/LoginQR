package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTAccessTokenAuthorizationRequest {
    /**
     * The service url.
     */
    private final String serviceUrl;

    /**
     * The SecSign ID.
     */
    private final String secSignId;

    /**
     * The {@link SecSignIDRESTAuthenticationMethod} to use.
     */
    private final SecSignIDRESTAuthenticationMethod authenticationMethod;

    /**
     * The {@link Capability} array to request.
     */
    private final Capability[] capabilities;

    /**
     * Constructor for the request.
     * @param serviceUrl the service url
     * @param secSignId the SecSign ID
     * @param authenticationMethod the {@link SecSignIDRESTAuthenticationMethod} to use
     * @param capabilities the {@link Capability} array to request
     */
    public SecSignIDRESTAccessTokenAuthorizationRequest(String serviceUrl, String secSignId,
                                                        SecSignIDRESTAuthenticationMethod authenticationMethod, Capability... capabilities) {
        this.serviceUrl = serviceUrl;
        this.secSignId = secSignId;
        this.authenticationMethod = authenticationMethod;
        this.capabilities = capabilities;
    }

    /**
     * Convert the request to a {@link JSONObject}.
     * @return the request in a {@link JSONObject}
     * @throws JSONException thrown if a JSON error occurred
     */
    public JSONObject toJSONObject() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("secsignid", secSignId);
        object.put("authmethod", authenticationMethod.getName());
        object.put("serviceurl", serviceUrl);

        String capabilitiesString = "";
        for(int i = 0; i < capabilities.length; i++) {
            capabilitiesString += capabilities[i].getName();

            if (i != capabilities.length - 1) {
                capabilitiesString += ";";
            }
        }
        object.put("capabilities", capabilitiesString);

        return object;
    }

    public enum Capability {
        SEC_SIGN_ID_DEVICE ("SecSignIdDevice"),
        TOTP_SECRET ("TOTPSecret"),
        FIDO_DEVICE ("FIDODevice");

        /**
         * The name of the capability which is used in the REST-API.
         */
        private final String name;

        /**
         * Constructor for the capability.
         * @param name the name of the capability
         */
        Capability(String name) {
            this.name = name;
        }

        /**
         * Get the name of the capability which is used in the REST-API.
         * @return the name of the capability which is used in the REST-API
         */
        public String getName() {
            return name;
        }
    }
}
