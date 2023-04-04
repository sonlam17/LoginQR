package com.secsign.java.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;



public class SecSignRESTConnector {

	/**
     * Cache refresh time for the server version.
     * 30 * 60 * 1000 = 30 minutes
     */
    private final long CACHE_TIME_SERVER_VERSION = 30 * 60 * 1000;

    /**
     * The timestamp of the last cache of the server version.
     */
    private long lastServerVersionCacheTime;

    /**
     * The cached server version.
     */
    private SecSignIDServerVersion cachedServerVersion;

    private String serverUrl;
    
    public enum PluginType{
    	ATLASSIAN , //1
        NEXTCLOUD , //2
        SHIBBOLETH , //3
        CUSTOM , //4
        TYPO3 , //5
        ADFS, //6
        KEYCLOAK, //7
        
        
    }
  
    private String pinAccountPassword;
    private String pinAccountUser;

 
    /**
     * public constructor with PinAccount
     */
    public SecSignRESTConnector(String serverURL,String pinAccountUser, String pinAccountPassword)
    {
        this.serverUrl=serverURL;
        this.pinAccountPassword=pinAccountPassword;
        this.pinAccountUser=pinAccountUser;
    }

    /**
     * Test the given url if we can connect to a running SecSign ID Server
     * @param url the url which can be a SecSign ID Server
     * @return true if we could connect to the ID Server
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public boolean testSecSignIDServerUrl(String url) throws SecSignIDRESTException
    {
        boolean connected = false;

        // https://stackoverflow.com/questions/15336477/deprecated-java-httpclient-how-hard-can-it-be
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(5000)
                .setConnectTimeout(5000)
                .setSocketTimeout(5000)
                .build();
        CloseableHttpClient httpClient = HttpClients.custom().useSystemProperties().setDefaultRequestConfig(requestConfig).build();



        // https://stackoverflow.com/questions/19797601/apache-http-basicscheme-authenticate-deprecated
        try
        {
            HttpGet httpGet = new HttpGet(url);
            httpClient.execute(httpGet);
            connected = true;
        }
        catch (Exception e)
        {
            System.out.println("Exception on testSecSignIDServerUrl: " + e.getMessage());
            throw new SecSignIDRESTException("Exception on testSecSignIDServerUrl: " + e.getMessage());
        }
        finally
        {
            try
            {
                httpClient.close();
            }
            catch (IOException e)
            {
                //already closed
            }
        }
        return connected;
    }

    /**
     * Get the device information for the specified SecSign ID.
     * @param secSignID the SecSign ID
     * @return the {@link SecSignIDRESTDevicesResponse}
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public SecSignIDRESTDevicesResponse getDevicesOfSecSignID(String secSignID) throws SecSignIDRESTException
    {
        System.out.println("Get devices of SecSign ID '" + secSignID + "'.");

        SecSignIDServerVersion serverVersion = getServerVersion();
        String endpointUrl;
        if (serverVersion.isGreaterOrEquals(SecSignIDServerVersion.VERSION_10_7_5)) {
            endpointUrl = "/rest/v1/Device/" + secSignID.toLowerCase() + "/Count";
        } else {
            endpointUrl = "/rest/v2/SecSignId/" + secSignID.toLowerCase() + "?devices";
        }
        SecSignIDRESTResponse response = getGetResponse(endpointUrl, null);

        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                return SecSignIDRESTDevicesResponse.fromJson(response.getContent(), secSignID, serverVersion);
            } catch (JSONException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("JSONException: " + e.getMessage());
            }
        }

        throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
    }

    /**
     * Check if a SecSign ID exists.
     * @param secSignID the secsign id to check
     * @return true if the SecSign ID exists
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public boolean checkSecSignID(String secSignID) throws SecSignIDRESTException
    {
        System.out.println("Check if SecSign ID '" + secSignID + "' exists.");

        String endpointUrl = "/rest/v2/SecSignId/" + secSignID.toLowerCase() + "?exist";
        SecSignIDRESTResponse response = getGetResponse(endpointUrl, null);

        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                SecSignIDRESTExistResponse existResponse = SecSignIDRESTExistResponse.fromJson(response.getContent());
                return existResponse.getExist();
            } catch (JSONException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("JSONException: " + e.getMessage());
            }
        }

        throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
    }

    /**
     * Creates SecSign ID on the SecSign ID server.
     * @param secSignId the SecSign ID to be created
     * @param email the email address of the SecSign ID
     * @return the {@link SecSignIDRESTCreateSecSignIDResponse}
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public SecSignIDRESTCreateSecSignIDResponse createSecSignID(String secSignId, String email) throws SecSignIDRESTException {
        String endpointUrl = "/rest/v2/SecSignId/";

        try {
            JSONObject requestObject = new JSONObject();
            requestObject.put("secsignid", secSignId);
            requestObject.put("email", email);
            requestObject.put("enable", "true");
            StringEntity entity = new StringEntity(requestObject.toString());
            SecSignIDRESTResponse response = getPostResponse(endpointUrl, entity, null);

            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return SecSignIDRESTCreateSecSignIDResponse.fromJson(response.getContent());
            }

            throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
        } catch(JSONException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("JSONException: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("UnsupportedEncodingException: " + e.getMessage());
        }
    }

    /**
     * Create the specified SecSign ID on the SecSign ID server.
     * @param secSignId the SecSign ID to be created
     * @param accessToken the access token for the request. Can be null if no access token is required.
     * @return the {@link SecSignIDRESTRestorationResponse}
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public SecSignIDRESTRestorationResponse getRestoreQRCode(String secSignId, String accessToken) throws SecSignIDRESTException {
        String endpointUrl = "/rest/v1/SecSignId/" + secSignId + "?restoration";
        if (accessToken != null) {
            endpointUrl += "&accesstoken=" + accessToken;
        }
        SecSignIDRESTResponse response = getGetResponse(endpointUrl, null);

        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                return SecSignIDRESTRestorationResponse.fromJson(response.getContent(), secSignId);
            } catch (JSONException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("JSONException: " + e.getMessage());
            }
        }

        throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
    }

    /**
     * Get the QR code to start the mobile app and create a SecSign ID within that app. No SecSign ID will be created on the server.
     * @param secSignId the SecSign ID
     * @return the {@link SecSignIDRESTCreateQRCodeResponse}
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public SecSignIDRESTCreateQRCodeResponse getCreateSecSignIDQrCode(String secSignId) throws SecSignIDRESTException {
        String endpointUrl = "/rest/v2/SecSignId/" + secSignId + "?createqrcode";
        SecSignIDRESTResponse response = getGetResponse(endpointUrl, null);

        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                return SecSignIDRESTCreateQRCodeResponse.fromJson(response.getContent());
            } catch (JSONException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("JSONException: " + e.getMessage());
            }
        }

        throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
    }

    /**
     * Start a Mail OTP authentication for the specified SecSign ID and email.
     * @param secSignId the SecSign ID
     * @param mail the email to send the OTP to
     * @return true if successful, otherwise false
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public boolean startMailAuth(String secSignId, String mail) throws SecSignIDRESTException {
        System.out.println("Starting mail auth for SecSign ID '" + secSignId + "' and mail '" + mail + "'.");

        String encodedSecSignId;
        String encodedMail;

        try {
            encodedSecSignId = URLEncoder.encode(secSignId, "UTF-8");
            encodedMail = URLEncoder.encode(mail, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("UnsupportedEncodingException: " + e.getMessage());
        }

        String endpointUrl = "/rest/v2/OTP?secsignid=" + encodedSecSignId.toLowerCase() + "&email=" + encodedMail;
        SecSignIDRESTResponse response = getGetResponse(endpointUrl, null);

        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return true;
        }

        throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
    }

    /**
     * Verify the Mail OTP for the specified SecSign ID.
     * @param secSignId the SecSign ID
     * @param mailCode the OTP
     * @return the {@link SecSignIDRESTVerifyMailCodeResponse}
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public SecSignIDRESTVerifyMailCodeResponse verifyMailCode(String secSignId, String mailCode) throws SecSignIDRESTException {
        System.out.println("Verifying mail code for SecSign ID '" + secSignId + "' and mail code '" + mailCode + "'.");

        if (secSignId == null || secSignId.length() == 0) {
            throw new SecSignIDRESTException("No SecSign ID provided for Mail-OTP.");
        }

        String encodedSecSignId;
        String encodedMailCode;

        try {
            encodedSecSignId = URLEncoder.encode(secSignId, "UTF-8");
            encodedMailCode = URLEncoder.encode(mailCode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("UnsupportedEncodingException: " + e.getMessage());
        }

        String endpointUrl = "/rest/v2/OTP/valid/"+encodedMailCode+"?secsignid=" + encodedSecSignId.toLowerCase();
        SecSignIDRESTResponse response = getGetResponse(endpointUrl, null);

        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                return SecSignIDRESTVerifyMailCodeResponse.fromJson(response.getContent());
            } catch (JSONException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("JSONException: " + e.getMessage());
            }
        }

        throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
    }

    /**
     * Verify the TOTP for the specified SecSign ID.
     * @param secSignId the SecSign ID
     * @param otpCode the TOTP
     * @return true if the TOTP was correct, otherwise false
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public boolean verifyOTPCode(String secSignId, String otpCode) throws SecSignIDRESTException {
        System.out.println("Verifying OTP code for SecSign ID '" + secSignId + "' and OTP code '" + otpCode + "'.");

        String encodedSecSignId;
        String encodedOTPCode;

        try {
            encodedSecSignId = URLEncoder.encode(secSignId, "UTF-8");
            encodedOTPCode = URLEncoder.encode(otpCode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("UnsupportedEncodingException: " + e.getMessage());
        }

        String endpointUrl = "/rest/v2/TOTP/Verify/" + encodedOTPCode + "?secsignid=" + encodedSecSignId.toLowerCase();
        SecSignIDRESTResponse response = getGetResponse(endpointUrl, null);

        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                SecSignIDRESTVerifyOTPCodeResponse verifyOTPCodeResponse = SecSignIDRESTVerifyOTPCodeResponse.fromJson(response.getContent());
                return verifyOTPCodeResponse.getValid();
            } catch (JSONException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("JSONException: " + e.getMessage());
            }
        }

        throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
    }

    /**
     * Checks whether a access token is required for TOTP activation.
     * @param secSignId the SecSign ID
     * @return true if a access token is required, otherwise false
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public boolean isAccessTokenForTOTPRequired(String secSignId,String url) throws SecSignIDRESTException {
        if (getServerVersion().isLower(SecSignIDServerVersion.VERSION_10_7_5)) {
            return false;
        }

        SecSignIDRESTAccessTokenInfoResponse response = getAccessTokenInfo(secSignId,url);
        return !response.isAccessAllowedWithoutToken();
    }

    /**
     * Get the TOTP QR-Code. If TOTP is not activated, this call activates TOTP.
     * @param secSignId the SecSign ID
     * @return the response with the TOTP QR-Code
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public SecSignIDRESTTOTPResponse getTOTPQRCode(String secSignId, String accessToken) throws SecSignIDRESTException {
        SecSignIDServerVersion serverVersion = getServerVersion();
        if (serverVersion.isGreaterOrEquals(SecSignIDServerVersion.VERSION_10_7_5)) {
            // TOTP is automatically activated.
            return getTOTPQRCodeInformations(secSignId, accessToken, serverVersion);
        } else {
            SecSignIDRESTTOTPResponse response = getTOTPQRCodeInformations(secSignId, null, serverVersion);

            // Check if TOTP is already activated. If not activate it and try again
            if(response.getTotpKeyUri().equals("") && response.getTotpQRCodeBase64().equals("")) {
                activateTOTP(secSignId);
                response = getTOTPQRCodeInformations(secSignId, null, serverVersion);
            }

            return response;
        }
    }

    /**
     * Get the TOTP QR-Code without activating TOTP automatically.
     * @param secSignId the SecSign ID
     * @return the response with the TOTP QR-Code
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    private SecSignIDRESTTOTPResponse getTOTPQRCodeInformations(String secSignId, String accessToken, SecSignIDServerVersion serverVersion) throws SecSignIDRESTException {
        String encodedSecSignId;

        try {
            encodedSecSignId = URLEncoder.encode(secSignId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("UnsupportedEncodingException: " + e.getMessage());
        }

        String endpointUrl;
        if (serverVersion.isGreaterOrEquals(SecSignIDServerVersion.VERSION_10_7_5)) {
            endpointUrl = "/rest/v2/TOTP?secsignid=" + encodedSecSignId.toLowerCase();
            if (accessToken != null) {
                endpointUrl += "&accesstoken=" + accessToken;
            }
        } else {
            endpointUrl = "/rest/v1/SecSignId/"+ encodedSecSignId.toLowerCase()+"?totp&json";
        }
        SecSignIDRESTResponse response = getGetResponse(endpointUrl, null);

        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                return SecSignIDRESTTOTPResponse.fromJson(response.getContent(), secSignId, serverVersion);
            } catch (JSONException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("JSONException: " + e.getMessage());
            }
        }

        throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
    }

    /**
     * Activate TOTP. If no manual activation is needed, this call does nothing.
     * @param secSignId the SecSign ID
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    private void activateTOTP(String secSignId) throws SecSignIDRESTException {
        String encodedSecSignId;

        try {
            encodedSecSignId = URLEncoder.encode(secSignId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("UnsupportedEncodingException: " + e.getMessage());
        }

        SecSignIDServerVersion serverVersion = getServerVersion();
        if (serverVersion.isLower(SecSignIDServerVersion.VERSION_10_7_5)) {
            String endpointUrl = "/rest/v1/SecSignId/" + encodedSecSignId.toLowerCase();

            try {
                JSONObject requestObject = new JSONObject();
                requestObject.put("update", "totpsecret");
                requestObject.put("generatetotpsecret", true);
                StringEntity entity = new StringEntity(requestObject.toString());

                SecSignIDRESTResponse response = getPostResponse(endpointUrl, entity, null);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    return;
                }

                throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
            } catch (JSONException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("JSONException: " + e.getMessage());
            } catch (UnsupportedEncodingException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("UnsupportedEncodingException: " + e.getMessage());
            }
        }
    }

    /**
     * Gets an authentication session for a SecSign ID user.
     * @param secSignId the secsign id
     * @param serviceName the service name e.g. Confluence or Jira
     * @param serviceAddress the service address meaning the url of the service
     * @param showaccesspassicons shall access pass icons be shown in the SecSign ID app
     * @return the {@link SecSignIDRESTCreateAuthSessionResponse}
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public SecSignIDRESTCreateAuthSessionResponse getAuthSession(String secSignId, String serviceName, String serviceAddress, boolean showaccesspassicons) throws SecSignIDRESTException
    {
        return this.getAuthSession(secSignId, serviceName, serviceAddress, showaccesspassicons, null, null, null);
    }

    /**
     * Gets an authentication session for a SecSign ID user.
     * @param secSignId the secsign id
     * @param serviceName the service name e.g. Confluence or Jira
     * @param serviceAddress the service address meaning the url of the service
     * @param showaccesspassicons shall access pass icons be shown in the SecSign ID app
     * @param transactionTitle The transaction title. If set no access pass will be displayed and the transaction title is shown in the push notification. Both parameter must be set: transactiontitle and transactiondescr. Can be null.
     * @param transactionDescription The transaction description. If set no access pass will be displayed. Will be shown in the app when the user accepts (or denies) the transaction. Both parameter must be set: transactiontitle and transactiondescr. Can be null.
     * @param transactionData The transaction data. It is an list of keys and values e.g.: key1=value1;key2=values2;. Can be null.
     * @return the {@link SecSignIDRESTCreateAuthSessionResponse}
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public SecSignIDRESTCreateAuthSessionResponse getAuthSession(String secSignId, String serviceName, String serviceAddress, boolean showaccesspassicons, String transactionTitle, String transactionDescription, String transactionData) throws SecSignIDRESTException {
        String encodedParameters;

        try {
            String encodedSecSignId = URLEncoder.encode(secSignId, "UTF-8");
            String encodedServiceName = URLEncoder.encode(serviceName, "UTF-8");
            String encodedServiceAddress = URLEncoder.encode(serviceAddress, "UTF-8");
            encodedParameters = "secsignid=" + encodedSecSignId + "&servicename=" + encodedServiceName +
                    "&serviceaddress=" + encodedServiceAddress +"&showaccesspassicons=" + showaccesspassicons;

            if (transactionTitle != null) {
                String encodedTransactionTitle = URLEncoder.encode(transactionTitle, "UTF-8");
                encodedParameters += "&transactiontitle=" + encodedTransactionTitle;
            }

            if (transactionDescription != null) {
                String encodedTransactionDescription = URLEncoder.encode(transactionDescription, "UTF-8");
                encodedParameters += "&transactiondescr=" +  encodedTransactionDescription;
            }

            if (transactionData != null) {
                String encodedTransactionData = URLEncoder.encode(transactionData, "UTF-8");
                encodedParameters += "&transactiondata=" + encodedTransactionData;
            }
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("UnsupportedEncodingException: " + e.getMessage());
        }

        String endpointUrl = "/qrcode/?content=" + encodedParameters;
        System.out.println("endpointUrl is "+endpointUrl );
        SecSignIDRESTResponse response = getGetResponse(endpointUrl, null);

        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                return SecSignIDRESTCreateAuthSessionResponse.fromJson(response.getContent());
            } catch (JSONException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("JSONException: " + e.getMessage());
            }
        } else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_CONFLICT) {
            return SecSignIDRESTCreateAuthSessionResponse.FROZEN;
        }

        throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
    }

    /**
     * Check the current state of the specified auth session.
     * @param authSessionId the auth session id
     * @return the {@link SecSignIDRESTCheckAuthSessionStateResponse} which contains the state of the specified auth session
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public SecSignIDRESTCheckAuthSessionStateResponse checkAuth(String authSessionId) throws SecSignIDRESTException {
        String endpointUrl = "/rest/v2/AuthSession/" + authSessionId;
        SecSignIDRESTResponse response = getGetResponse(endpointUrl, null);

        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                return SecSignIDRESTCheckAuthSessionStateResponse.fromJson(response.getContent());
            } catch (JSONException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("JSONException: " + e.getMessage());
            }
        }

        throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
    }

    /**
     * Cancel the specfied auth session.
     * @param authSessionId the auth session id
     * @return true if the auth session was successfully canceled, otherwise false
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public boolean cancelAuthSession(String authSessionId) throws SecSignIDRESTException {
        String endpointUrl = "/rest/v2/AuthSession/" + authSessionId;
        SecSignIDRESTResponse response = getDeleteResponse(endpointUrl, null);

        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return true;
        }

        throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
    }

    /**
     * Register the plugin to create an plugin account.
     * @return the {@link SecSignIDRESTPluginRegistrationResponse}
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public SecSignIDRESTPluginRegistrationResponse registerPlugin(String url, String siteName, String pluginName, PluginType pluginType) throws SecSignIDRESTException {
        UsernamePasswordCredentials credentials = getHttpBasicAuthCredentials();

        try {
            JSONObject requestObject = new JSONObject();
            requestObject.put("url", url);
            requestObject.put("siteName", siteName);
            requestObject.put("pluginName", pluginName);
            switch(pluginType)
            {
			
			case ATLASSIAN:
				requestObject.put("pluginType", "1");
				break;
			case NEXTCLOUD:
				requestObject.put("pluginType", "2");
				break;
			case SHIBBOLETH:
				requestObject.put("pluginType", "3");
				break;
			case CUSTOM:
				requestObject.put("pluginType", "4");
				break;
			case ADFS:
				requestObject.put("pluginType", "5");
				break;
			case TYPO3:
				requestObject.put("pluginType", "6");
				break;
			case KEYCLOAK:
				requestObject.put("pluginType", "7");
				break;
			default:
				break;
            }
            
            
            StringEntity entity = new StringEntity(requestObject.toString());

            SecSignIDRESTResponse response = getPostResponse("/rest/v2/PluginRegistration", entity, credentials);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return SecSignIDRESTPluginRegistrationResponse.fromJson(response.getContent());
            }

            throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
        } catch (JSONException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("JSONException: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("UnsupportedEncodingException: " + e.getMessage());
        }
    }

    /**
     * Update the plugin registration.
     * @param secSignId the SecSign ID. Can manage all plugins later. Can be null if not changed
     * @param email the email. Informed when e.g. updates are available. Can be null if not changed
     * @param pluginName the complete name of the plugin including the version number. Can be null if not changed
     * @param url the public URL of the website using this plugin. Can be null if not changed
     */
    public void updatePluginRegistration(String secSignId, String email, String pluginName, String url) throws SecSignIDRESTException {
        String endpointUrl = "/rest/v2/PluginRegistration/Update";
        SecSignIDRESTPluginRegistrationUpdateRequest request = new SecSignIDRESTPluginRegistrationUpdateRequest(secSignId,
                email, pluginName, url);

        try {
            StringEntity entity = new StringEntity(request.toJSONObject().toString());

            SecSignIDRESTResponse response = getPostResponse(endpointUrl, entity, null);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return;
            }

            throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
        } catch (JSONException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("JSONException: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("UnsupportedEncodingException: " + e.getMessage());
        }
    }

    /**
     * Get the server version of the SecSign ID Server.
     * @return the server version. If the SecSign ID Server doesn't support this endpoint,
     *          {@link SecSignIDServerVersion#VERSION_UNKNOWN} is returned
     */
    public SecSignIDServerVersion getServerVersion() {
        boolean cacheExpired = (System.currentTimeMillis() - lastServerVersionCacheTime) >= CACHE_TIME_SERVER_VERSION;
        if (cachedServerVersion != null && !cacheExpired) {
            System.out.println("Using cached server version " + cachedServerVersion.toString());
            return cachedServerVersion;
        }

        String endpointUrl = "/rest/v2/PluginRegistration/ServerInfo";

        SecSignIDServerVersion serverVersion = SecSignIDServerVersion.VERSION_UNKNOWN;
        try {
            SecSignIDRESTResponse response = getGetResponse(endpointUrl, null);
            SecSignIDRESTServerInfoResponse serverInfoResponse = SecSignIDRESTServerInfoResponse.fromJson(response.getContent());
            serverVersion = serverInfoResponse.getVersion();
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        } catch (SecSignIDRESTException e) {
            // Only warn. This is expected behaviour before ID Server 10.7.5.
            System.out.println(e.getMessage());
        }

        cachedServerVersion = serverVersion;
        lastServerVersionCacheTime = System.currentTimeMillis();

        return serverVersion;
    }

    /**
     * Get the access token info.
     * @param secSignId the SecSign ID
     * @return the {@link SecSignIDRESTAccessTokenInfoResponse}
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public SecSignIDRESTAccessTokenInfoResponse getAccessTokenInfo(String secSignId,String serviceURL) throws SecSignIDRESTException {
        if (getServerVersion().isLower(SecSignIDServerVersion.VERSION_10_7_5)) {
            return SecSignIDRESTAccessTokenInfoResponse.NO_ACCESS_TOKEN_RESPONSE;
        }
        

        try {
            JSONObject requestObject = new JSONObject();
            requestObject.put("secsignid", secSignId);
            requestObject.put("serviceurl", serviceURL);
            StringEntity entity = new StringEntity(requestObject.toString());

            SecSignIDRESTResponse response = getPostResponse("/rest/v2/AccessToken/Info", entity, null);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return SecSignIDRESTAccessTokenInfoResponse.fromJson(response.getContent());
            }

            throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
        } catch (JSONException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("JSONException: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("UnsupportedEncodingException: " + e.getMessage());
        }
    }

    /**
     * Request a access token authorization.
     * @param secSignId the SecSign ID
     * @param authenticationMethod the authentication method of the authorization
     * @param capability the capability to request
     * @return the {@link SecSignIDRESTAccessTokenAuthorizationResponse}
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public SecSignIDRESTAccessTokenAuthorizationResponse requestAccessTokenAuthorization(String secSignId, SecSignIDRESTAuthenticationMethod authenticationMethod, SecSignIDRESTAccessTokenAuthorizationRequest.Capability capability,String url) throws SecSignIDRESTException {
        
    	SecSignIDRESTAccessTokenAuthorizationRequest request = new SecSignIDRESTAccessTokenAuthorizationRequest(url, secSignId, authenticationMethod, capability);

        try {
            StringEntity entity = new StringEntity(request.toJSONObject().toString());

            SecSignIDRESTResponse response = getPostResponse("/rest/v2/AccessToken", entity, null);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return SecSignIDRESTAccessTokenAuthorizationResponse.fromJson(response.getContent());
            }

            throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
        } catch (JSONException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("JSONException: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("UnsupportedEncodingException: " + e.getMessage());
        }
    }

    /**
     * Request a access token.
     * @param request the {@link SecSignIDRESTAccessTokenRequest}
     * @return the {@link SecSignIDRESTAccessTokenResponse}
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public SecSignIDRESTAccessTokenResponse requestAccessToken(SecSignIDRESTAccessTokenRequest request) throws SecSignIDRESTException {
        try {
            StringEntity entity = new StringEntity(request.toJSONObject().toString());

            SecSignIDRESTResponse response = getPostResponse("/rest/v2/AccessToken/" + request.getTokenId(), entity, null);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return SecSignIDRESTAccessTokenResponse.fromJson(response.getContent());
            }

            throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
        } catch (JSONException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("JSONException: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("UnsupportedEncodingException: " + e.getMessage());
        }
    }

    /**
     * Start a FIDO registration.
     * @param secSignId the SecSign ID
     * @param credentialNickname the credential nickname
     * @param accessToken the access token
     * @return the {@link SecSignIDRESTFIDORegisterStartResponse}
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public SecSignIDRESTFIDORegisterStartResponse startFIDORegister(String secSignId, String credentialNickname, String accessToken,String url) throws SecSignIDRESTException {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            String rpId = getTopDomainFromURI(uri);
            if (rpId == null) {
                rpId = host;
            }
            String rpName = host;

            JSONObject requestObject = new JSONObject();
            requestObject.put("rpId", rpId);
            requestObject.put("rpName", rpName);
            requestObject.put("userName", secSignId);
            requestObject.put("credentialNickname", credentialNickname);
            requestObject.put("accesstoken", accessToken);
            StringEntity entity = new StringEntity(requestObject.toString());

            SecSignIDRESTResponse response = getPostResponse("/rest/v2/FIDO/Register/Start", entity, null);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return SecSignIDRESTFIDORegisterStartResponse.fromJson(response.getContent());
            }

            throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
        } catch (JSONException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("JSONException: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("UnsupportedEncodingException: " + e.getMessage());
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("URISyntaxException: " + e.getMessage());
        }
    }

    /**
     * Finish a FIDO registration.
     * @param secSignId the SecSign ID
     * @param credentialId the credential ID
     * @param clientDataJson the client data JSON
     * @param attestationObject the attestation object
     * @param accessToken the access token. Can be null if not required
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public void finishFIDORegister(String secSignId, String credentialId, String clientDataJson, String attestationObject, String accessToken) throws SecSignIDRESTException {
        try {
            JSONObject requestObject = new JSONObject();
            requestObject.put("userName", secSignId);
            requestObject.put("credentialId", credentialId);
            requestObject.put("clientDataJSON", clientDataJson);
            requestObject.put("attestationObject", attestationObject);
            requestObject.put("accesstoken", accessToken);
            StringEntity entity = new StringEntity(requestObject.toString());

            SecSignIDRESTResponse response = getPostResponse("/rest/v2/FIDO/Register/Finish", entity, null);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return;
            }

            throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
        } catch (JSONException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("JSONException: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("UnsupportedEncodingException: " + e.getMessage());
        }
    }

    /**
     * Start a FIDO authentication
     * @param secSignId the SecSign ID
     * @return the {@link SecSignIDRESTFIDOAuthenticateStartResponse}
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public SecSignIDRESTFIDOAuthenticateStartResponse startFIDOAuthenticate(String secSignId,String url) throws SecSignIDRESTException {
        try {
        	URI uri = new URI(url);
            String rpId = getTopDomainFromURI(uri);
            if (rpId == null) {
                rpId = uri.getHost();;
            }

            JSONObject requestObject = new JSONObject();
            requestObject.put("rpId", rpId);
            requestObject.put("userName", secSignId);
            StringEntity entity = new StringEntity(requestObject.toString());

            SecSignIDRESTResponse response = getPostResponse("/rest/v2/FIDO/Authenticate/Start", entity, null);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return SecSignIDRESTFIDOAuthenticateStartResponse.fromJson(response.getContent());
            }
            else {
            	if(response.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN)
            	{
            		if(!rpId.equals(uri.getHost()))
            		{
            			rpId = uri.getHost();
            			requestObject = new JSONObject();
        	            requestObject.put("rpId", rpId);
        	            requestObject.put("userName", secSignId);
        	            entity = new StringEntity(requestObject.toString());

        	            response = getPostResponse("/rest/v2/FIDO/Authenticate/Start", entity, null);
        	            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        	                return SecSignIDRESTFIDOAuthenticateStartResponse.fromJson(response.getContent());
        	            }
            		}
            	}
            }

            throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
        } catch (JSONException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("JSONException: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("UnsupportedEncodingException: " + e.getMessage());
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("URISyntaxException: " + e.getMessage());
        }
    }

    /**
     * Finish a FIDO authentication
     * @param secSignId the SecSign ID
     * @param credentialId the credential ID
     * @param clientDataJson the client data JSON
     * @param authenticatorData the authenticator data
     * @param signature the signature
     * @param userHandle the user handle
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    public void finishFIDOAuthenticate(String secSignId, String credentialId, String clientDataJson,
                                              String authenticatorData, String signature, String userHandle) throws SecSignIDRESTException {
        try {
            JSONObject requestObject = new JSONObject();
            requestObject.put("userName", secSignId);
            requestObject.put("credentialId", credentialId);
            requestObject.put("clientDataJSON", clientDataJson);
            requestObject.put("authenticatorData", authenticatorData);
            requestObject.put("signature", signature);
            requestObject.put("userHandle", userHandle);
            StringEntity entity = new StringEntity(requestObject.toString());

            SecSignIDRESTResponse response = getPostResponse("/rest/v2/FIDO/Authenticate/Finish", entity, null);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return;
            }

            throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
        } catch (JSONException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("JSONException: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("UnsupportedEncodingException: " + e.getMessage());
        }
    }

    /**
     * Gets username password credentials for HTTP Basic Auth for REST call of a secSign ID Server resource
     * @return username password credentials
     */
    private UsernamePasswordCredentials getHttpBasicAuthCredentials() {
        // a pin account is a pair of a username and a password which will be used for Http Basic Auth
    	return new UsernamePasswordCredentials(pinAccountUser, pinAccountPassword);
    }

    /**
     * Get the GET response for the specified endpoint.
     * @param endpointUrl the endpoint url
     * @param credentials the credentials
     * @return the {@link SecSignIDRESTResponse}
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    private SecSignIDRESTResponse getGetResponse(String endpointUrl, UsernamePasswordCredentials credentials) throws SecSignIDRESTException {
        try {
            HttpResponse response = null;
            try {
                response = executeGet(endpointUrl, credentials);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("Cannot reach ID server.");
            } catch (AuthenticationException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("REST AuthenticationException: " + e.getMessage());
            }

            StatusLine status = response.getStatusLine();
            String content = null;
            try {
                content = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("REST IOException: " + e.getMessage());
            }

            System.out.println(endpointUrl + " [" + status.getStatusCode() + "] " + String.valueOf(content));
            return SecSignIDRESTResponse.createSuccessResponse(status, content);
        } catch (SecSignIDRESTException e) {
            // Just throw REST exceptions.
            throw e;
        } catch (Exception e) {
            // Some not excepted exception occurred.
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("REST Exception: " + e.getMessage());
        }
    }

    /**
     * Get the POST response for the specified endpoint.
     * @param endpointUrl the endpoint url
     * @param entity the {@link HttpEntity} for the request body
     * @param credentials the credentials
     * @return the {@link SecSignIDRESTResponse}
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    private SecSignIDRESTResponse getPostResponse(String endpointUrl, HttpEntity entity, UsernamePasswordCredentials credentials) throws SecSignIDRESTException {
        try {
            HttpResponse response = null;
            try {
                response = executePost(endpointUrl, entity, credentials);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("Cannot reach ID server.");
            } catch (AuthenticationException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("REST AuthenticationException: " + e.getMessage());
            }

            StatusLine status = response.getStatusLine();
            String content = null;
            try {
                content = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("REST IOException: " + e.getMessage());
            }

            System.out.println(endpointUrl + " [" + status.getStatusCode() + "] " + String.valueOf(content));
            return SecSignIDRESTResponse.createSuccessResponse(status, content);
        } catch (SecSignIDRESTException e) {
            // Just throw REST exceptions.
            throw e;
        } catch (Exception e) {
            // Some not excepted exception occurred.
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("REST Exception: " + e.getMessage());
        }
    }

    /**
     * Get the DELETE response for the specified endpoint.
     * @param endpointUrl the endpoint url
     * @param credentials the credentials
     * @return the {@link SecSignIDRESTResponse}
     * @throws SecSignIDRESTException thrown if a error occurred
     */
    private SecSignIDRESTResponse getDeleteResponse(String endpointUrl, UsernamePasswordCredentials credentials) throws SecSignIDRESTException {
        try {
            HttpResponse response = null;
            try {
                response = executeDelete(endpointUrl, credentials);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("Cannot reach ID server.");
            } catch (AuthenticationException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("REST AuthenticationException: " + e.getMessage());
            }

            StatusLine status = response.getStatusLine();
            String content = null;
            try {
                content = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new SecSignIDRESTException("REST IOException: " + e.getMessage());
            }

            System.out.println(endpointUrl + " [" + status.getStatusCode() + "] " + String.valueOf(content));
            return SecSignIDRESTResponse.createSuccessResponse(status, content);
        } catch (SecSignIDRESTException e) {
            // Just throw REST exceptions.
            throw e;
        } catch (Exception e) {
            // Some not excepted exception occurred.
            System.out.println(e.getMessage());
            throw new SecSignIDRESTException("REST Exception: " + e.getMessage());
        }
    }

    /**
     * Create a {@link HttpGet} request for the specified server and endpoint.
     * @param serverUrl the server url
     * @param endpointUrl the endpoint url
     * @param credentials the credentials
     * @return the {@link HttpGet} request
     * @throws AuthenticationException thrown if the credentials couldn't be added to the request
     */
    private HttpGet createHttpGet(String serverUrl, String endpointUrl, UsernamePasswordCredentials credentials) throws AuthenticationException {
        HttpGet httpGet = new HttpGet(serverUrl + endpointUrl);
        addRequiredHeadersToRequest(httpGet, credentials);

        return httpGet;
    }

    /**
     * Create a {@link HttpPost} request for the specified server and endpoint.
     * @param serverUrl the server url
     * @param endpointUrl the endpoint url
     * @param credentials the credentials
     * @return the {@link HttpPost} request
     * @throws AuthenticationException thrown if the credentials couldn't be added to the request
     */
    private HttpPost createHttpPost(String serverUrl, String endpointUrl, UsernamePasswordCredentials credentials) throws AuthenticationException {
        HttpPost httpPost = new HttpPost(serverUrl + endpointUrl);
        addRequiredHeadersToRequest(httpPost, credentials);

        return httpPost;
    }

    /**
     * Create a {@link HttpDelete} request for the specified server and endpoint.
     * @param serverUrl the server url
     * @param endpointUrl the endpoint url
     * @param credentials the credentials
     * @return the {@link HttpDelete} request
     * @throws AuthenticationException thrown if the credentials couldn't be added to the request
     */
    private HttpDelete createHttpDelete(String serverUrl, String endpointUrl, UsernamePasswordCredentials credentials) throws AuthenticationException {
        HttpDelete httpDelete = new HttpDelete(serverUrl + endpointUrl);
        addRequiredHeadersToRequest(httpDelete, credentials);

        return httpDelete;
    }

    /**
     * Add all required headers to the {@link HttpRequest}.
     * @param request the {@link HttpRequest}
     * @param credentials the credentials
     * @throws AuthenticationException thrown if the credentials couldn't be added to the request
     */
    private void addRequiredHeadersToRequest(HttpRequest request, UsernamePasswordCredentials credentials) throws AuthenticationException {
        Header basicAuthHeader;
        if (credentials == null) {
            basicAuthHeader = new BasicScheme(StandardCharsets.UTF_8)
                    .authenticate(getHttpBasicAuthCredentials(), request, null);
        } else {
            basicAuthHeader = new BasicScheme(StandardCharsets.UTF_8)
                    .authenticate(credentials, request, null);
        }

        request.addHeader(basicAuthHeader);
        request.addHeader("Content-type", "application/json");
    }

    /**
     * Executes a HTTP GET on the specified endpoint. If the request fails, the fallback server is used.
     * @param endpointUrl the endpoint url
     * @param credentials the credentials
     * @return the {@link HttpResponse}
     * @throws IOException
     * @throws AuthenticationException
     */
    private HttpResponse executeGet(String endpointUrl, UsernamePasswordCredentials credentials) throws IOException, AuthenticationException {
    	CloseableHttpClient httpClient = HttpClientBuilder.create().useSystemProperties().build();
       

        try {
        	
            return executeGetForServer(httpClient, serverUrl, endpointUrl, credentials);
        } catch(IOException e) {
            System.out.println("Cannot reach server with URL '" + serverUrl + "'.");
            System.out.println(e.getMessage());
            throw e;
        }
    }

    /**
     * Executes a HTTP POST on the specified endpoint. If the request fails, the fallback server is used.
     * @param endpointUrl the endpoint url
     * @param credentials the credentials
     * @return the {@link HttpResponse}
     * @throws IOException
     * @throws AuthenticationException
     */
    private HttpResponse executePost(String endpointUrl, HttpEntity entity, UsernamePasswordCredentials credentials) throws IOException, AuthenticationException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().useSystemProperties().build();
      

        try {
            return executePostForServer(httpClient, serverUrl, endpointUrl, entity, credentials);
        } catch(IOException e) {
            System.out.println("Cannot reach server with URL '" + serverUrl + "'.");
            System.out.println(e.getMessage());
            throw e;
        }
    }

    /**
     * Executes a HTTP DELETE on the specified endpoint. If the request fails, the fallback server is used.
     * @param endpointUrl the endpoint url
     * @param credentials the credentials
     * @return the {@link HttpResponse}
     * @throws IOException
     * @throws AuthenticationException
     */
    private HttpResponse executeDelete(String endpointUrl, UsernamePasswordCredentials credentials) throws IOException, AuthenticationException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().useSystemProperties().build();
      

        try {
            return executeDeleteForServer(httpClient, serverUrl, endpointUrl, credentials);
        } catch(IOException e) {
            System.out.println("Cannot reach server with URL '" + serverUrl + "'.");
            System.out.println(e.getMessage());
            throw e;
        }
    }

    /**
     * Executes a HTTP GET on the specified server and endpoint.
     * @param httpClient the client
     * @param serverUrl the server url
     * @param endpointUrl the endpoint url
     * @param credentials the credentials
     * @return the {@link HttpResponse}
     * @throws IOException
     * @throws AuthenticationException
     */
    private HttpResponse executeGetForServer(HttpClient httpClient, String serverUrl, String endpointUrl, UsernamePasswordCredentials credentials) throws IOException, AuthenticationException {
    	System.out.println("Before CreatGet "+serverUrl +" credentials " +credentials);
    	HttpGet httpGet = createHttpGet(serverUrl, endpointUrl, credentials);
        System.out.println("Call: " + httpGet.getMethod() + " on " + httpGet.getURI().toString());
        return httpClient.execute(httpGet);
    }

    /**
     * Executes a HTTP POST on the specified server and endpoint.
     * @param httpClient the client
     * @param serverUrl the server url
     * @param endpointUrl the endpoint url
     * @param credentials the credentials
     * @return the {@link HttpResponse}
     * @throws IOException
     * @throws AuthenticationException
     */
    private HttpResponse executePostForServer(HttpClient httpClient, String serverUrl, String endpointUrl, HttpEntity entity, UsernamePasswordCredentials credentials) throws IOException, AuthenticationException {
        HttpPost httpPost = createHttpPost(serverUrl, endpointUrl, credentials);
        httpPost.setEntity(entity);
        System.out.println("Call: " + httpPost.getMethod() + " on " + httpPost.getURI().toString());
        return httpClient.execute(httpPost);
    }

    /**
     * Executes a HTTP DELETE on the specified server and endpoint.
     * @param httpClient the client
     * @param serverUrl the server url
     * @param endpointUrl the endpoint url
     * @param credentials the credentials
     * @return the {@link HttpResponse}
     * @throws IOException
     * @throws AuthenticationException
     */
    private HttpResponse executeDeleteForServer(HttpClient httpClient, String serverUrl, String endpointUrl, UsernamePasswordCredentials credentials) throws IOException, AuthenticationException {
        HttpDelete httpDelete = createHttpDelete(serverUrl, endpointUrl, credentials);
        System.out.println("Call: " + httpDelete.getMethod() + " on " + httpDelete.getURI().toString());
        return httpClient.execute(httpDelete);
    }

    /**
     * Get a {@link SecSignIDRESTException} for the specified status code
     * @param statusCode the status code
     * @return the {@link SecSignIDRESTException} for the status code
     */
    private SecSignIDRESTException getExceptionForStatusCode(int statusCode) {
    	
        if (statusCode == HttpStatus.SC_NOT_FOUND) {
            return new SecSignIDRESTException("REST-Call not found. See log for details.");
        } else if (statusCode == HttpStatus.SC_FORBIDDEN) {
            return new SecSignIDRESTException("Unauthorized REST-Call. Check ID-Server rights.");
        } else if (statusCode == HttpStatus.SC_BAD_REQUEST) {
            return new SecSignIDRESTException("Bad Request. Try login again.");
        }

        return new SecSignIDRESTException("Error on calling Server REST API");
    }

    private String getTopDomainFromURI(URI uri) {
        return new SecSignIDPublicSuffixInterpreter().getRegisteredDomain(uri);
    }

   
	
}
