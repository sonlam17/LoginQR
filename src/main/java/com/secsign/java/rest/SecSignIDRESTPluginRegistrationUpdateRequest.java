package com.secsign.java.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class SecSignIDRESTPluginRegistrationUpdateRequest {
    /**
     * The SecSign ID. Can manage all plugins later. Can be null if not changed.
     */
    private final String secSignId;

    /**
     * The email. Informed when e.g. updates are available. Can be null if not changed.
     */
    private final String email;

    /**
     * The complete name of the plugin including the version number. Can be null if not changed.
     */
    private final String pluginName;

    /**
     * The public URL of the website using this plugin. Can be null if not changed.
     */
    private final String url;

    /**
     * Constructor.
     * @param secSignId the SecSign ID. Can manage all plugins later. Can be null if not changed
     * @param email the email. Informed when e.g. updates are available. Can be null if not changed
     * @param pluginName the complete name of the plugin including the version number. Can be null if not changed
     * @param url the public URL of the website using this plugin. Can be null if not changed
     */
    public SecSignIDRESTPluginRegistrationUpdateRequest(String secSignId, String email, String pluginName, String url) {
        this.secSignId = secSignId;
        this.email = email;
        this.pluginName = pluginName;
        this.url = url;
    }

    /**
     * Convert the request to a {@link JSONObject}.
     * @return the request in a {@link JSONObject}
     * @throws JSONException thrown if a JSON error occurred
     */
    public JSONObject toJSONObject() throws JSONException {
        JSONObject object = new JSONObject();
        if (secSignId != null) {
            object.put("secSignId", secSignId);
        }

        if (email != null) {
            object.put("email", email);
        }

        if (pluginName != null) {
            object.put("pluginName", pluginName);
        }

        if (url != null) {
            object.put("url", url);
        }

        return object;
    }
}
