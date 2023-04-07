package com.secsign.java.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;


public class Connector {

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
  


 
    /**
     * public constructor with PinAccount
     */
    public Connector(String serverURL)
    {
        this.serverUrl=serverURL;
    }

    /**
     * Test the given url if we can connect to a running SecSign ID Server
     * @param url the url which can be a SecSign ID Server
     * @return true if we could connect to the ID Server
     * @throws Exception thrown if a error occurred
     */
    public boolean testSecSignIDServerUrl(String url) throws Exception
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
        catch (java.lang.Exception e)
        {
            System.out.println("Exception on testSecSignIDServerUrl: " + e.getMessage());
            throw new Exception("Exception on testSecSignIDServerUrl: " + e.getMessage());
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
     * @throws Exception thrown if a error occurred
     */

    /**
     * Check if a SecSign ID exists.
     * @param secSignID the secsign id to check
     * @return true if the SecSign ID exists
     * @throws Exception thrown if a error occurred
     */

    /**
     * Creates SecSign ID on the SecSign ID server.
     * @param secSignId the SecSign ID to be created
     * @param email the email address of the SecSign ID
     * @return the {@link SecSignIDRESTCreateSecSignIDResponse}
     * @throws Exception thrown if a error occurred
     */

    /**
     * Get the QR code to start the mobile app and create a SecSign ID within that app. No SecSign ID will be created on the server.
     * @param secSignId the SecSign ID
     * @return the {@link CreateQRCodeResponse}
     * @throws Exception thrown if a error occurred
     */
    public CreateQRCodeResponse getCreateSecSignIDQrCode(String secSignId) throws Exception {
        String endpointUrl = "/qrcode/?content=" + secSignId;
        Response response = getGetResponse(endpointUrl, null);

        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                return CreateQRCodeResponse.fromJson(response.getContent());
            } catch (JSONException e) {
                System.out.println(e.getMessage());
                throw new Exception("JSONException: " + e.getMessage());
            }
        }

        throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
    }

//    /**
//     * Start a Mail OTP authentication for the specified SecSign ID and email.
//     * @param secSignId the SecSign ID
//     * @param mail the email to send the OTP to
//     * @return true if successful, otherwise false
//     * @throws SecSignIDRESTException thrown if a error occurred
//     */

    public CreateAuthSessionResponse getAuthSession(String serviceName, String serviceAddress, boolean showaccesspassicons) throws Exception
    {
        return this.getAuthSession(serviceName, serviceAddress, showaccesspassicons, null, null, null);
    }

    /**
     * Gets an authentication session for a SecSign ID user.
     * @param serviceName the service name e.g. Confluence or Jira
     * @param serviceAddress the service address meaning the url of the service
     * @param showaccesspassicons shall access pass icons be shown in the SecSign ID app
     * @param transactionTitle The transaction title. If set no access pass will be displayed and the transaction title is shown in the push notification. Both parameter must be set: transactiontitle and transactiondescr. Can be null.
     * @param transactionDescription The transaction description. If set no access pass will be displayed. Will be shown in the app when the user accepts (or denies) the transaction. Both parameter must be set: transactiontitle and transactiondescr. Can be null.
     * @param transactionData The transaction data. It is an list of keys and values e.g.: key1=value1;key2=values2;. Can be null.
     * @return the {@link CreateAuthSessionResponse}
     * @throws Exception thrown if a error occurred
     */
    public CreateAuthSessionResponse getAuthSession(String serviceName, String serviceAddress, boolean showaccesspassicons, String transactionTitle, String transactionDescription, String transactionData) throws Exception {
        String encodedParameters;

        try {

            String encodedServiceName = URLEncoder.encode(serviceName, "UTF-8");
            String encodedServiceAddress = URLEncoder.encode(serviceAddress, "UTF-8");
            encodedParameters = "secsignid=" +  "&servicename=" + encodedServiceName +
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
            throw new Exception("UnsupportedEncodingException: " + e.getMessage());
        }

        String endpointUrl = "/qrcode/?content=" + encodedParameters;
        System.out.println("endpointUrl is "+endpointUrl );
        Response response = getGetResponse(endpointUrl, null);

        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                System.out.println("--------------------");
                System.out.println(response.getContent());
                return CreateAuthSessionResponse.fromJson(response.getContent());
            } catch (JSONException e) {
                System.out.println(e.getMessage());
                throw new Exception("JSONException: " + e.getMessage());
            }
        } else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_CONFLICT) {
            return CreateAuthSessionResponse.FROZEN;
        }

        throw getExceptionForStatusCode(response.getStatusLine().getStatusCode());
    }




    /**
     * Get the GET response for the specified endpoint.
     * @param endpointUrl the endpoint url
     * @param credentials the credentials
     * @return the {@link Response}
     * @throws Exception thrown if a error occurred
     */
    private Response getGetResponse(String endpointUrl, UsernamePasswordCredentials credentials) throws Exception {
        try {
            HttpResponse response = null;
            try {
                response = executeGet(endpointUrl, credentials);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new Exception("Cannot reach ID server.");
            } catch (AuthenticationException e) {
                System.out.println(e.getMessage());
                throw new Exception("REST AuthenticationException: " + e.getMessage());
            }

            StatusLine status = response.getStatusLine();
            String content = null;
            try {
                content = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new Exception("REST IOException: " + e.getMessage());
            }

            System.out.println(endpointUrl + " [" + status.getStatusCode() + "] " + String.valueOf(content));
            return Response.createSuccessResponse(status, content);
        } catch (Exception e) {
            // Just throw REST exceptions.
            throw e;
        }
    }

    /**
     * Get the POST response for the specified endpoint.
     * @param endpointUrl the endpoint url
     * @param entity the {@link HttpEntity} for the request body
     * @param credentials the credentials
     * @return the {@link Response}
     * @throws Exception thrown if a error occurred
     */
    private Response getPostResponse(String endpointUrl, HttpEntity entity, UsernamePasswordCredentials credentials) throws Exception {
        try {
            HttpResponse response = null;
            try {
                response = executePost(endpointUrl, entity, credentials);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new Exception("Cannot reach ID server.");
            } catch (AuthenticationException e) {
                System.out.println(e.getMessage());
                throw new Exception("REST AuthenticationException: " + e.getMessage());
            }

            StatusLine status = response.getStatusLine();
            String content = null;
            try {
                content = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new Exception("REST IOException: " + e.getMessage());
            }

            System.out.println(endpointUrl + " [" + status.getStatusCode() + "] " + String.valueOf(content));
            return Response.createSuccessResponse(status, content);
        } catch (Exception e) {
            // Just throw REST exceptions.
            throw e;
        }
    }

    /**
     * Get the DELETE response for the specified endpoint.
     * @param endpointUrl the endpoint url
     * @param credentials the credentials
     * @return the {@link Response}
     * @throws Exception thrown if a error occurred
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
//            basicAuthHeader = new BasicScheme(StandardCharsets.UTF_8)
//                    .authenticate(getHttpBasicAuthCredentials(), request, null);
        } else {
//            basicAuthHeader = new BasicScheme(StandardCharsets.UTF_8)
//                    .authenticate(credentials, request, null);
        }

//        request.addHeader(basicAuthHeader);
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
     * Get a {@link Exception} for the specified status code
     * @param statusCode the status code
     * @return the {@link Exception} for the status code
     */
    private Exception getExceptionForStatusCode(int statusCode) {
    	
        if (statusCode == HttpStatus.SC_NOT_FOUND) {
            return new Exception("REST-Call not found. See log for details.");
        } else if (statusCode == HttpStatus.SC_FORBIDDEN) {
            return new Exception("Unauthorized REST-Call. Check ID-Server rights.");
        } else if (statusCode == HttpStatus.SC_BAD_REQUEST) {
            return new Exception("Bad Request. Try login again.");
        }

        return new Exception("Error on calling Server REST API");
    }


   
	
}
