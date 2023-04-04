package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTPluginRegistrationResponse {
    /**
     * Logger for this class.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecSignIDRESTPluginRegistrationResponse.class);

    /**
     * The account name.
     */
    private final String accountName;

    /**
     * The password.
     */
    private final String password;

    /**
     * Creates a response from JSON.
     * @param json the json of this response
     * @return the response
     * @throws JSONException thrown when a JSON error occurred
     * @throws SecSignIDRESTException thrown when a JSON key was not found
     */
    public static SecSignIDRESTPluginRegistrationResponse fromJson(String json) throws JSONException, SecSignIDRESTException {
        JSONObject rootObject = new JSONObject(json);
        String accountName = rootObject.optString("accountName", null);
        if (accountName == null) {
            logger.debug("Key 'accountName' not found");
            throw new SecSignIDRESTException("Key 'accountName' not found");
        }

        String password = rootObject.optString("password", null);
        if (password == null) {
            logger.debug("Key 'password' not found");
            throw new SecSignIDRESTException("Key 'password' not found");
        }

        return new SecSignIDRESTPluginRegistrationResponse(accountName, password);
    }

    /**
     * Constructor for the response.
     * @param accountName the account name
     * @param password the password
     */
    private SecSignIDRESTPluginRegistrationResponse(String accountName, String password) {
        this.accountName = accountName;
        this.password = password;
    }

    /**
     * Get the account name.
     * @return the account name
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Get the password.
     * @return the password
     */
    public String getPassword() {
        return password;
    }
}
