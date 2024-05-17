package com.secsign.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;

import com.secsign.keycloak.authenticator.QrUtilities;
import com.secsign.model.QrModel;
import com.secsign.representation.QrRepresentation;
import com.secsign.service.QrService;
import com.secsign.util.QRCodeGenerator;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;


public class Connector {
    private final QrService qrService;
    private static Logger logger = Logger.getLogger(QrUtilities.class);

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

    private static String serverUrl;
    public enum MethodType{
        POST,
        DELETE,
        GET
    }
    
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
    public Connector(QrService qrService, String serverURL)
    {
        this.qrService = qrService;
        this.serverUrl=serverURL;
    }

//    /**
//     * Start a Mail OTP authentication for the specified SecSign ID and email.
//     * @param secSignId the SecSign ID
//     * @param mail the email to send the OTP to
//     * @return true if successful, otherwise false
//     * @throws SecSignIDRESTException thrown if a error occurred
//     */
    public static QrLoginResponse pollQrLoginStatus(AuthenticationFlowContext context, String qrLoginId) {
        final String methodName = "pollQrLoginStatus";
        SecurityVerifyLoggingUtilities.entry(logger, methodName, context, qrLoginId);

        String userId=null;
        QrLoginResponse qrResponse = null;
        try {
            String endpointUrl = "realms/"+context.getRealm().getId()+"/qr/"+qrLoginId;
            System.out.println("endpointUrl is " + endpointUrl);
            Response response = getResponse(endpointUrl, MethodType.GET);
            System.out.println("endpointUrl is " + response.getContent());
            JSONObject data = new JSONObject(response.getContent());
            Boolean state = Boolean.valueOf(data.optString("state", null));
            userId = data.optString("userId", null);
            qrResponse = new QrLoginResponse(state,userId);
        } catch (Exception e) {
            e.printStackTrace();
        }//		IBMSecurityVerifyLoggingUtilities.exit(logger, methodName, qrResponse);
        return qrResponse;
    }
    public void deleteQr(AuthenticationFlowContext context, String qrLoginId) {
        final String methodName = "deleteQr";
        SecurityVerifyLoggingUtilities.entry(logger, methodName, null, qrLoginId);
        try {
            String endpointUrl = "/qrcode/deleteQr?qrId=" + qrLoginId;
            System.out.println("endpointUrl is " + endpointUrl);
            Response response = getResponse(endpointUrl, MethodType.DELETE);
            System.out.println(response.getContent());
//            JSONObject rootObject = new JSONObject(response.getContent());
//            System.out.println(rootObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public UserModel matchCIUserIdToUserModel(AuthenticationFlowContext context, String userId) throws Exception {
        final String methodName = "matchCIUserIdToUserModel";

        String kcUserId = getKCUserId(context, userId);
        UserModel matchingUser = null;

        if (kcUserId != null) {
            List<UserModel> users = context.getSession().users().getUsers(context.getRealm());
            UserModel iterUser;
            String id;
            for (int i = 0; i < users.size(); i++) {
                iterUser = users.get(i);
                id = iterUser.getId();
                if (kcUserId.equals(id)) {
                    matchingUser = iterUser;
                    i = users.size();
                }
            }
        } else {
            // TODO: Error - mismatch / user does not exist
        }

//		IBMSecurityVerifyLoggingUtilities.exit(logger, methodName, matchingUser != null ? matchingUser.toString() : null);
        return matchingUser;
    }
    public String getKCUserId(AuthenticationFlowContext context, String ciUserId) throws Exception {
//        final String methodName = "getKCUserId";
////		IBMSecurityVerifyLoggingUtilities.entry(logger, methodName, ciUserId);
//
//        String tenantHostname = SecurityVerifyUtilities.getTenantHostname(context);
//        String accessToken = SecurityVerifyUtilities.getAccessToken(context);
//        String kcUserId = null;
//        CloseableHttpClient httpClient = null;
//        String endpointUrl = "/qrcode/getQrCode";
//        System.out.println("endpointUrl is "+endpointUrl );
//        Response response = getGetResponse(endpointUrl);
//        try {
//            httpClient = HttpClients.createDefault();
//            URI uri = new URIBuilder()
//                    .setScheme("https")
//                    .setHost(tenantHostname)
//                    .setPath("/v2.0/Users")
//                    .setParameter("fullText", ciUserId)
//                    .build();
//            HttpGet getRequest = new HttpGet(uri);
//            getRequest.addHeader("Authorization", "Bearer " + accessToken);
//            getRequest.addHeader("Accept", "application/scim+json");
//            CloseableHttpResponse response = httpClient.execute(getRequest);
//            int statusCode = response.getStatusLine().getStatusCode();
//            String responseBody = EntityUtils.toString(response.getEntity());
//            EntityUtils.consume(response.getEntity());
//            if (statusCode == 200) {
//                Pattern idExtraction = Pattern.compile("\"externalId\":\"([a-f0-9\\-]+)\"");
//                Matcher matcher = idExtraction.matcher(responseBody);
//                if (matcher.find()) {
//                    kcUserId = matcher.group(1);
//                }
//            } else {
////				IBMSecurityVerifyLoggingUtilities.error(logger, methodName, String.format("%s: %s", statusCode, responseBody));
//            }
//            response.close();
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        } catch (ClientProtocolException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (httpClient != null) {
//                try {
//                    httpClient.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
////		IBMSecurityVerifyLoggingUtilities.exit(logger, methodName, kcUserId);
//        return kcUserId;
        return ciUserId;
    }
    /**
     * Gets an authentication session for a SecSign ID user.
      * @return the {@link CreateAuthSessionResponse}
     * @throws Exception thrown if a error occurred
     */
    public CreateAuthSessionResponse getAuthSession(AuthenticationFlowContext context) throws Exception {
        QrRepresentation qrRepresentation = new QrRepresentation();
        qrRepresentation.setContent(serverUrl);
        qrRepresentation.setState(false);
        QrModel qrModel = qrService.createQr(qrRepresentation);
        System.out.println("--------------------");
        System.out.println(qrModel.getContent());
        byte[] image = QRCodeGenerator.getQRCodeImage(qrModel.getContent(), 300, 300);
        String qrcodeContent = Base64.getEncoder().encodeToString(image);
        try {
            return CreateAuthSessionResponse.fromJson(qrModel.getId(), qrcodeContent, context);
        } catch (JSONException e) {
            System.out.println(e.getMessage());
            throw new Exception("JSONException: " + e.getMessage());
        }
    }




    /**
     * Get the GET response for the specified endpoint.
     * @param endpointUrl the endpoint url
     * @return the {@link Response}
     * @throws Exception thrown if a error occurred
     */
    private static Response getResponse(String endpointUrl, MethodType type) throws Exception {
        try {
            HttpResponse response = null;
            try {
                response = execute(endpointUrl, type);
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
            return Response.createSuccessResponse(status, content);
        } catch (Exception e) {
            // Just throw REST exceptions.
            throw e;
        }
    }

    /**
     * Get the DELETE response for the specified endpoint.
     * @param endpointUrl the endpoint url
     * @return the {@link Response}
     * @throws Exception thrown if a error occurred
     */

    private static HttpGet createHttpGet(String serverUrl, String endpointUrl) throws AuthenticationException {
        HttpGet httpGet = new HttpGet(serverUrl + endpointUrl);
        addRequiredHeadersToRequest(httpGet);

        return httpGet;
    }
    private static HttpDelete createHttpDelete(String serverUrl, String endpointUrl) throws AuthenticationException {
        HttpDelete httpDelete = new HttpDelete(serverUrl + endpointUrl);
        addRequiredHeadersToRequest(httpDelete);

        return httpDelete;
    }


    /**
     * Create a {@link HttpDelete} request for the specified server and endpoint.
     * @param serverUrl the server url
     * @param endpointUrl the endpoint url
     * @param credentials the credentials
     * @return the {@link HttpDelete} request
     * @throws AuthenticationException thrown if the credentials couldn't be added to the request
     */

    /**
     * Add all required headers to the {@link HttpRequest}.
     * @param request the {@link HttpRequest}
     * @throws AuthenticationException thrown if the credentials couldn't be added to the request
     */
    private static void addRequiredHeadersToRequest(HttpRequest request) throws AuthenticationException {
        request.addHeader("Content-type", "application/json");
    }

    /**
     * Executes a HTTP GET on the specified endpoint. If the request fails, the fallback server is used.
     *
     * @param endpointUrl the endpoint url
     * @return the {@link HttpResponse}
     * @throws IOException
     * @throws AuthenticationException
     */
    private static HttpResponse execute(String endpointUrl, MethodType Type) throws IOException, AuthenticationException {
    	CloseableHttpClient httpClient = HttpClientBuilder.create().useSystemProperties().build();
        try {
            switch (Type){
                case GET:
                    return executeGetForServer(httpClient, serverUrl, endpointUrl);
//                case POST:
//                    return executeDelForServer(httpClient, serverUrl, endpointUrl);
                case DELETE:
                    return executeDelForServer(httpClient, serverUrl, endpointUrl);
                default:
                    return null;
            }
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
     * @return the {@link HttpResponse}
     * @throws IOException
     * @throws AuthenticationException
     */
    private static HttpResponse executeGetForServer(HttpClient httpClient, String serverUrl, String endpointUrl) throws IOException, AuthenticationException {
    	System.out.println("Before CreatGet "+serverUrl );
    	HttpGet httpGet = createHttpGet(serverUrl, endpointUrl);
        System.out.println("Call: " + httpGet.getMethod() + " on " + httpGet.getURI().toString());
        return httpClient.execute(httpGet);
    }
    private static HttpResponse executeDelForServer(HttpClient httpClient, String serverUrl, String endpointUrl) throws IOException, AuthenticationException {
        System.out.println("Before CreatGet "+serverUrl );
        HttpDelete httpDelete = createHttpDelete(serverUrl, endpointUrl);
        System.out.println("Call: " + httpDelete.getMethod() + " on " + httpDelete.getURI().toString());
        return httpClient.execute(httpDelete);
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
