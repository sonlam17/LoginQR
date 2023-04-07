package com.secsign.keycloak.authenticator;

import com.secsign.java.rest.SecurityVerifyLoggingUtilities;
import com.secsign.java.rest.SecurityVerifyUtilities;
import com.secsign.java.rest.QrLoginResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QrUtilities {
	private static Logger logger = Logger.getLogger(QrUtilities.class);
	
	public static final String DEFAULT_SERVER = "http://183.91.3.60:8080/sca-test";
	private static final String KEYCLOAK_PIN_ACCOUNT = "KEYCLOAK_ADDON";
	private static final String KEYCLOAK_PIN_ACCOUNT_PASSWORD = "?22OhZeFFq(k6AX.fde00";
	private static String serverURL=null;
	private static String pinAccountUser=null;
	private static String pinAccountPassword=null;

//	public static SecSignRESTConnector getRESTConnector() throws NullPointerException
//    {
//		SecSignRESTConnector connector=null;
//		if(serverURL!=null)
//		{
//			if(pinAccountUser==null || pinAccountUser.equals(""))
//			{
//				connector = new SecSignRESTConnector(serverURL,KEYCLOAK_PIN_ACCOUNT,KEYCLOAK_PIN_ACCOUNT_PASSWORD);
//			}else {
//				connector = new SecSignRESTConnector(serverURL,pinAccountUser,pinAccountPassword);
//			}
//
//		}else {
//			throw new NullPointerException("No serverURL on creating Connector");
//		}
//
//    	return connector;
//    }

	public static void saveServerURL(String pServerURL) {
		serverURL=pServerURL;
	}

	public static void setPinAccount(String ppinAccountUser, String ppinAccountPassword) {
		pinAccountUser=ppinAccountUser;
		pinAccountPassword=ppinAccountPassword;
	}

	public static boolean hasPinAccount() {
		return pinAccountPassword!=null && pinAccountUser!=null;
	}
	public static String getQrLoginId(AuthenticationFlowContext context) {
		final String methodName = "getQrLoginId";

		String result = context.getAuthenticationSession().getUserSessionNotes().get("qr.login.id");


		return result;
	}
	public static QrLoginResponse pollQrLoginStatus(AuthenticationFlowContext context, String qrLoginId, String qrLoginDsi) {
		final String methodName = "pollQrLoginStatus";
		SecurityVerifyLoggingUtilities.entry(logger, methodName, context, qrLoginId, qrLoginDsi);

		String tenantHostname = SecurityVerifyUtilities.getTenantHostname(context);
		String accessToken = SecurityVerifyUtilities.getAccessToken(context);
		CloseableHttpClient httpClient = null;
		QrLoginResponse qrResponse = null;
		try {
			httpClient = HttpClients.createDefault();
			URI uri = new URIBuilder()
					.setScheme("https")
					.setHost(tenantHostname)
					.setPath("/v2.0/factors/qr/authenticate/" + qrLoginId)
					.setParameter("dsi", qrLoginDsi)
					.build();
			HttpGet getRequest = new HttpGet(uri);
			getRequest.addHeader("Authorization", "Bearer " + accessToken);
			getRequest.addHeader("Accept", "application/json");
			CloseableHttpResponse response = httpClient.execute(getRequest);
			int statusCode = response.getStatusLine().getStatusCode();
			String responseBody = EntityUtils.toString(response.getEntity());
			EntityUtils.consume(response.getEntity());
			if (statusCode == 200) {
				String state = null;
				Pattern stateExtraction = Pattern.compile("\"state\":\\s*\"([a-zA-Z]+)\"");
				Matcher matcher = stateExtraction.matcher(responseBody);
				if (matcher.find()) {
					state = matcher.group(1);
				}
				String userId = null;
				Pattern userIdExtraction = Pattern.compile("\"userId\":\\s*\"([a-zA-Z0-9]+)\"");
				matcher = userIdExtraction.matcher(responseBody);
				if (matcher.find()) {
					userId = matcher.group(1);
				}
				qrResponse = new QrLoginResponse(state, userId);
			} else {
				SecurityVerifyLoggingUtilities.error(logger, methodName, String.format("%s: %s", statusCode, responseBody));
			}
			response.close();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

//		IBMSecurityVerifyLoggingUtilities.exit(logger, methodName, qrResponse);
		return qrResponse;
	}
	public static UserModel matchCIUserIdToUserModel(AuthenticationFlowContext context, String userId) {
		final String methodName = "matchCIUserIdToUserModel";
//		IBMSecurityVerifyLoggingUtilities.entry(logger, methodName, context, userId);

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
	public static String getKCUserId(AuthenticationFlowContext context, String ciUserId) {
		final String methodName = "getKCUserId";
//		IBMSecurityVerifyLoggingUtilities.entry(logger, methodName, ciUserId);

		String tenantHostname = SecurityVerifyUtilities.getTenantHostname(context);
		String accessToken = SecurityVerifyUtilities.getAccessToken(context);
		String kcUserId = null;
		CloseableHttpClient httpClient = null;
		try {
			httpClient = HttpClients.createDefault();
			URI uri = new URIBuilder()
					.setScheme("https")
					.setHost(tenantHostname)
					.setPath("/v2.0/Users")
					.setParameter("fullText", ciUserId)
					.build();
			HttpGet getRequest = new HttpGet(uri);
			getRequest.addHeader("Authorization", "Bearer " + accessToken);
			getRequest.addHeader("Accept", "application/scim+json");
			CloseableHttpResponse response = httpClient.execute(getRequest);
			int statusCode = response.getStatusLine().getStatusCode();
			String responseBody = EntityUtils.toString(response.getEntity());
			EntityUtils.consume(response.getEntity());
			if (statusCode == 200) {
				Pattern idExtraction = Pattern.compile("\"externalId\":\"([a-f0-9\\-]+)\"");
				Matcher matcher = idExtraction.matcher(responseBody);
				if (matcher.find()) {
					kcUserId = matcher.group(1);
				}
			} else {
//				IBMSecurityVerifyLoggingUtilities.error(logger, methodName, String.format("%s: %s", statusCode, responseBody));
			}
			response.close();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

//		IBMSecurityVerifyLoggingUtilities.exit(logger, methodName, kcUserId);
		return kcUserId;
	}
	public static void setQrLoginId(AuthenticationFlowContext context, String qrLoginId) {
		final String methodName = "setQrLoginId";
		SecurityVerifyLoggingUtilities.entry(logger, methodName, context, qrLoginId);

		context.getAuthenticationSession().setUserSessionNote("qr.login.id", qrLoginId);

		SecurityVerifyLoggingUtilities.exit(logger, methodName);
	}

	public static String getQrLoginDsi(AuthenticationFlowContext context) {
		final String methodName = "getQrLoginDsi";
		SecurityVerifyLoggingUtilities.entry(logger, methodName, context);

		String result = context.getAuthenticationSession().getUserSessionNotes().get("qr.login.dsi");

		SecurityVerifyLoggingUtilities.exit(logger, methodName, result);
		return result;
	}

	public static void setQrLoginDsi(AuthenticationFlowContext context, String qrLoginDsi) {
		final String methodName = "setQrLoginDsi";
		SecurityVerifyLoggingUtilities.entry(logger, methodName, context, qrLoginDsi);

		context.getAuthenticationSession().setUserSessionNote("qr.login.dsi", qrLoginDsi);

		SecurityVerifyLoggingUtilities.exit(logger, methodName);
	}

	public static String getQrLoginImage(AuthenticationFlowContext context) {
		final String methodName = "getQrLoginImage";
		SecurityVerifyLoggingUtilities.entry(logger, methodName, context);

		String result = context.getAuthenticationSession().getUserSessionNotes().get("qr.login.image");

		SecurityVerifyLoggingUtilities.exit(logger, methodName, result);
		return result;
	}

	public static void setQrLoginImage(AuthenticationFlowContext context, String qrLoginImage) {
		final String methodName = "setQrLoginImage";
		SecurityVerifyLoggingUtilities.entry(logger, methodName, context, qrLoginImage);

		context.getAuthenticationSession().setUserSessionNote("qr.login.image", qrLoginImage);

		SecurityVerifyLoggingUtilities.exit(logger, methodName);
	}
}
