package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTRestorationResponse {
    /**
     * Logger for this class.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecSignIDRESTRestorationResponse.class);

    /**
     * The SecSign ID used by the response.
     */
    private final String secSignId;

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
     * @param secSignId the SecSign ID of the response
     * @return the response
     * @throws JSONException thrown when a JSON error occurred
     * @throws SecSignIDRESTException thrown when a JSON key was not found
     */
    public static SecSignIDRESTRestorationResponse fromJson(String json, String secSignId) throws JSONException, SecSignIDRESTException {
        JSONObject rootObject = new JSONObject(json);
        if (!rootObject.has(secSignId)) {
            logger.debug("Key '" + secSignId + "' not found");
            throw new SecSignIDRESTException("No secsignid information available");
        }

        JSONObject secSignObject = rootObject.getJSONObject(secSignId);
        if (!secSignObject.has("restoration")) {
            logger.debug("Key 'restoration' not found");
            throw new SecSignIDRESTException("No totp information available");
        }

        JSONObject restorationObject = secSignObject.getJSONObject("restoration");
        String restoreUrl = restorationObject.optString("restoreurl", null);
        if (restoreUrl == null) {
            logger.debug("Key 'restoreurl' not found");
            throw new SecSignIDRESTException("No restoreurl information available");
        }

        String qrCodeBase64 = restorationObject.optString("qrcodebase64", null);
        if (qrCodeBase64 == null) {
            logger.debug("Key 'qrcodebase64' not found");
            throw new SecSignIDRESTException("No qrcodebase64 information available");
        }

        return new SecSignIDRESTRestorationResponse(secSignId, restoreUrl, qrCodeBase64);
    }

    /**
     * Constructor for the response.
     * @param secSignId the SecSign ID used by the response
     * @param restoreUrl the URL for the SecSign ID App to restore the SecSign ID
     * @param qrCodeBase64 the QR-Code to scan to restore the SecSign ID
     */
    private SecSignIDRESTRestorationResponse(String secSignId, String restoreUrl, String qrCodeBase64) {
        this.secSignId = secSignId;
        this.restoreUrl = restoreUrl;
        this.qrCodeBase64 = qrCodeBase64;
    }

    /**
     * Get the SecSign ID used by the response.
     * @return the SecSign ID used by the response
     */
    public String getSecSignId() {
        return secSignId;
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
