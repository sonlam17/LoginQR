package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTCreateQRCodeResponse {
    /**
     * Logger for this class.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecSignIDRESTCreateQRCodeResponse.class);

    /**
     * The URL for the SecSign ID App to create the SecSign ID.
     */
    private final String createUrl;

    /**
     * The QR-Code to scan to create the SecSign ID.
     */
    private final String qrCodeBase64;

    /**
     * Creates a response from JSON.
     * @param json the json of this response
     * @return the response
     * @throws JSONException thrown when a JSON error occurred
     * @throws SecSignIDRESTException thrown when a JSON key was not found
     */
    public static SecSignIDRESTCreateQRCodeResponse fromJson(String json) throws JSONException, SecSignIDRESTException {
        JSONObject rootObject = new JSONObject(json);
        String createUrl = rootObject.optString("createurl", null);
        if (createUrl == null) {
            logger.debug("Key 'createurl' not found");
            throw new SecSignIDRESTException("Key 'createurl' not found");
        }

        String qrCodeBase64 = rootObject.optString("qrcodebase64", null);
        if (qrCodeBase64 == null) {
            logger.debug("Key 'qrcodebase64' not found");
            throw new SecSignIDRESTException("Key 'qrcodebase64' not found");
        }

        return new SecSignIDRESTCreateQRCodeResponse(createUrl, qrCodeBase64);
    }

    /**
     * Constructor for the response.
     * @param createUrl the URL for the SecSign ID App to create the SecSign ID.
     * @param qrCodeBase64 the QR-Code to scan to create the SecSign ID
     */
    private SecSignIDRESTCreateQRCodeResponse(String createUrl, String qrCodeBase64) {
        this.createUrl = createUrl;
        this.qrCodeBase64 = qrCodeBase64;
    }

    /**
     * Get the URL for the SecSign ID App to create the SecSign ID.
     * @return the URL for the SecSign ID App to create the SecSign ID
     */
    public String getCreateUrl() {
        return createUrl;
    }

    /**
     * Get the QR-Code to scan to create the SecSign ID.
     * @return the QR-Code to scan to create the SecSign ID
     */
    public String getQrCodeBase64() {
        return qrCodeBase64;
    }
}
