package com.secsign.java.rest;

public enum SecSignIDRESTAuthenticationMethod {
    SEC_SIGN_ID ("SecSignID"),
    TOTP ("TOTP"),
    FIDO ("FIDO"),
    MAIL_OTP ("MailOTP"),
    UNKNOWN ("unknown");

    /**
     * The name which is used in the REST-API.
     */
    private final String name;

    /**
     * Get the {@link SecSignIDRESTAuthenticationMethod} by the name.
     * @param name the name which is used in the REST-API
     * @return the {@link SecSignIDRESTAuthenticationMethod} or, if no {@link SecSignIDRESTAuthenticationMethod} was
     *      found with the specific name, {@link SecSignIDRESTAuthenticationMethod#UNKNOWN} is returned
     */
    public static SecSignIDRESTAuthenticationMethod fromName(String name) {
        if (SEC_SIGN_ID.getName().equals(name)) {
            return SEC_SIGN_ID;
        }
        if (TOTP.getName().equals(name)) {
            return TOTP;
        }
        if (FIDO.getName().equals(name)) {
            return FIDO;
        }
        if (MAIL_OTP.getName().equals(name)) {
            return MAIL_OTP;
        }

        return UNKNOWN;
    }

    /**
     * Constructor for the {@link SecSignIDRESTAuthenticationMethod}
     * @param name the name which is used in the REST-API
     */
    SecSignIDRESTAuthenticationMethod(String name) {
        this.name = name;
    }

    /**
     * Get the name which is used in the REST-API.
     * @return the name which is used in the REST-API
     */
    public String getName() {
        return name;
    }
}
