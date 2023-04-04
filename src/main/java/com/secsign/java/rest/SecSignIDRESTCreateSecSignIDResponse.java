package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTCreateSecSignIDResponse {
    /**
     * Logger for this class.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecSignIDRESTCreateSecSignIDResponse.class);

    /**
     * The URL for the SecSign ID App to restore the SecSign ID.
     */
    private final String restoreUrl;

    /**
     * The QR-Code to scan to restore the SecSign ID.
     */
    private final String qrCodeBase64;

    /**
     * Creates a response from JSON.
     * @param json the json of this response
     * @return the response
     * @throws JSONException thrown when a JSON error occurred
     * @throws SecSignIDRESTException thrown when a JSON key was not found
     */
    public static SecSignIDRESTCreateSecSignIDResponse fromJson(String json) throws JSONException, SecSignIDRESTException {
        JSONObject rootObject = new JSONObject(json);
        String createUrl = rootObject.optString("restoreurl", null);
        if (createUrl == null) {
            logger.debug("Key 'restoreurl' not found");
            throw new SecSignIDRESTException("Key 'restoreurl' not found");
        }

        String qrCodeBase64 = rootObject.optString("qrcodebase64", null);
        if (qrCodeBase64 == null) {
            logger.debug("Key 'qrcodebase64' not found");
            throw new SecSignIDRESTException("Key 'qrcodebase64' not found");
        }

        return new SecSignIDRESTCreateSecSignIDResponse(createUrl, qrCodeBase64);
    }

    /**
     * Constructor for the response.
     * @param restoreUrl the URL for the SecSign ID App to restore the SecSign ID
     * @param qrCodeBase64 the QR-Code to scan to restore the SecSign ID
     */
    private SecSignIDRESTCreateSecSignIDResponse(String restoreUrl, String qrCodeBase64) {
        this.restoreUrl = restoreUrl;
        this.qrCodeBase64 = qrCodeBase64;
    }

    /**
     * Get the URL for the SecSign ID App to restore the SecSign ID.
     * @return the URL for the SecSign ID App to restore the SecSign ID
     */
    public String getRestoreUrl() {
        return restoreUrl;
    }

    /**
     * Get the QR-Code to scan to restore the SecSign ID.
     * @return the QR-Code to scan to restore the SecSign ID
     */
    public String getQrCodeBase64() {
        return qrCodeBase64;
    }
}
