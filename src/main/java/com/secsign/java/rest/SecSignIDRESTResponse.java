package com.secsign.java.rest;

import org.apache.http.StatusLine;

public class SecSignIDRESTResponse {
    /**
     * The success state of the response.
     */
    private final boolean success;

    /**
     * The status line of the response.
     */
    private final StatusLine statusLine;

    /**
     * The response content.
     */
    private final String content;

    /**
     * Creates a failed response.
     * @return the response
     */
    public static SecSignIDRESTResponse createFailedResponse() {
        return new SecSignIDRESTResponse(false, null, null);
    }

    /**
     * Create a success response
     * @param statusLine the status line
     * @param content the content
     * @return the response
     */
    public static SecSignIDRESTResponse createSuccessResponse(StatusLine statusLine, String content) {
        return new SecSignIDRESTResponse(true, statusLine, content);
    }

    /**
     * Constructor for the response.
     * @param success the success state
     * @param statusLine the status line
     * @param content the content
     */
    private SecSignIDRESTResponse(boolean success, StatusLine statusLine, String content) {
        this.success = success;
        this.statusLine = statusLine;
        this.content = content;
    }

    /**
     * Get the success state of the response.
     * @return the success state of the response
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * Get the status line of the response.
     * @return the status line of the response
     */
    public StatusLine getStatusLine() {
        return statusLine;
    }

    /**
     * The response content.
     * @return the response content.
     */
    public String getContent() {
        return content;
    }
}
