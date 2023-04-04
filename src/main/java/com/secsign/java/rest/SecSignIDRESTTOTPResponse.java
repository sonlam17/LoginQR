package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTTOTPResponse {
    /**
     * Logger for this class.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecSignIDRESTTOTPResponse.class);

    /**
     * The TOTP key uri.
     */
    private final String totpKeyUri;

    /**
     * The TOTP key uri as an QR-Code
     */
    private final String totpQRCodeBase64;

    /**
     * Creates a response from JSON.
     * @param json the json of this response
     * @param secSignId the SecSign ID of the response
     * @return the response
     * @throws JSONException thrown when a JSON error occurred
     * @throws SecSignIDRESTException thrown when a JSON key was not found
     */
    public static SecSignIDRESTTOTPResponse fromJson(String json, String secSignId, SecSignIDServerVersion serverVersion) throws JSONException, SecSignIDRESTException {
        JSONObject rootObject = new JSONObject(json);

        if (serverVersion.isGreaterOrEquals(SecSignIDServerVersion.VERSION_10_7_5)) {
            return getResponseFromJsonObject(rootObject);
        }

        if (!rootObject.has(secSignId)) {
            logger.debug("Key '" + secSignId + "' not found");
            throw new SecSignIDRESTException("No secsignid information available");
        }

        JSONObject secSignObject = rootObject.getJSONObject(secSignId);
        if (!secSignObject.has("totp")) {
            logger.debug("Key 'totp' not found");
            throw new SecSignIDRESTException("No totp information available");
        }

        return getResponseFromJsonObject(secSignObject);
    }

    private static SecSignIDRESTTOTPResponse getResponseFromJsonObject(JSONObject object) throws JSONException, SecSignIDRESTException {
        JSONObject totpObject = object.getJSONObject("totp");
        String totpKeyUri = totpObject.optString("totpkeyuri", null);
        if (totpKeyUri == null) {
            logger.debug("Key 'totpkeyuri' not found");
            throw new SecSignIDRESTException("No totpkeyuri information available");
        }

        String totpQRCodeBase64 = totpObject.optString("totpqrcodebase64", null);
        if (totpQRCodeBase64 == null) {
            logger.debug("Key 'totpqrcodebase64' not found");
            throw new SecSignIDRESTException("No totpqrcodebase64 information available");
        }

        return new SecSignIDRESTTOTPResponse(totpKeyUri, totpQRCodeBase64);
    }

    /**
     * Constructor for the response.
     * @param totpKeyUri the TOTP key uri
     * @param totpQRCodeBase64 the TOTP key uri as an QR-Code
     */
    private SecSignIDRESTTOTPResponse(String totpKeyUri, String totpQRCodeBase64) {
        this.totpKeyUri = totpKeyUri;
        this.totpQRCodeBase64 = totpQRCodeBase64;
    }

    /**
     * Get the TOTP key uri.
     * @return the TOTP key uri
     */
    public String getTotpKeyUri() {
        return totpKeyUri;
    }

    /**
     * Get the TOTP key uri as an QR-Code
     * @return the TOTP key uri as an QR-Code
     */
    public String getTotpQRCodeBase64() {
        return totpQRCodeBase64;
    }
}
