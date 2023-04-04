package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTDevicesResponse {
    /**
     * Logger for this class.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecSignIDRESTDevicesResponse.class);

    /**
     * The Device count associated to the SecSign ID.
     */
    private final int deviceCount;

    /**
     * Creates a response from JSON.
     * @param json the json of this response
     * @param secSignId the SecSign ID of the response
     * @return the response
     * @throws JSONException thrown when a JSON error occurred
     * @throws SecSignIDRESTException thrown when a JSON key was not found
     */
    public static SecSignIDRESTDevicesResponse fromJson(String json, String secSignId, SecSignIDServerVersion serverVersion) throws JSONException, SecSignIDRESTException {
        JSONObject rootObject = new JSONObject(json);

        if (serverVersion.isGreaterOrEquals(SecSignIDServerVersion.VERSION_10_7_5)) {
            int deviceCount = rootObject.optInt("count", -1);
            if (deviceCount == -1) {
                logger.debug("Key 'count' not found");
                throw new SecSignIDRESTException("Key 'count' not found");
            }

            return new SecSignIDRESTDevicesResponse(deviceCount);
        }

        if (!rootObject.has(secSignId)) {
            logger.debug("Key '" + secSignId + "' not found");
            throw new SecSignIDRESTException("No secsignid information available");
        }

        JSONObject secSignObject = rootObject.getJSONObject(secSignId);
        if (!secSignObject.has("devices")) {
            logger.debug("Key 'devices' not found");
            throw new SecSignIDRESTException("No devices information available");
        }

        JSONObject devicesObject = secSignObject.getJSONObject("devices");
        return new SecSignIDRESTDevicesResponse(devicesObject.length());
    }

    /**
     * Constructor for the response.
     * @param deviceCount the Device count associated to the SecSign ID
     */
    private SecSignIDRESTDevicesResponse(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    /**
     * Get the Device count associated to the SecSign ID.
     * @return the Device count associated to the SecSign ID
     */
    public int getDeviceCount() {
        return deviceCount;
    }
}
