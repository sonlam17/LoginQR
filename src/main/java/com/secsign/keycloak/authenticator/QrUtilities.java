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
import org.keycloak.representations.AccessToken;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QrUtilities {
	private static Logger logger = Logger.getLogger(QrUtilities.class);
	
	public static final String DEFAULT_SERVER = "http://183.91.3.60:8080/sca-test";
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



	public static String getQrLoginId(AuthenticationFlowContext context) {
		final String methodName = "getQrLoginId";
		SecurityVerifyLoggingUtilities.entry(logger, methodName, context);

		String result = context.getAuthenticationSession().getUserSessionNotes().get("qr.login.id");

		SecurityVerifyLoggingUtilities.exit(logger, methodName, result);
		return result;
	}
	public static UserModel matchCIUserNameToUserModel(AuthenticationFlowContext context, String userName, String stringAccessToken) throws IOException, URISyntaxException {
		UserModel matchingUser = null;
//		CloseableHttpClient httpClient = HttpClients.createDefault();
//		URI uri = new URIBuilder()
//				.setScheme("https")
//				.setHost("https://keycloakproduction.com/iam/realms/demo/protocol/openid-connect/userinfo")
//				.build();
//		HttpGet getRequest = new HttpGet(uri);
//		getRequest.addHeader("Authorization", "Bearer " + stringAccessToken);
//		getRequest.addHeader("Accept", "application/json");
//		CloseableHttpResponse response = httpClient.execute(getRequest);
//		System.out.println(response);
		if (userName != null) {
			List<UserModel> users = context.getSession().users().getUsers(context.getRealm());
			UserModel iterUser;
			String userNameKeycloak;
			for (int i = 0; i < users.size(); i++) {
				iterUser = users.get(i);
				userNameKeycloak = iterUser.getUsername();
				if (userName.equals(userNameKeycloak)) {
					matchingUser = iterUser;
					i = users.size();
				}
			}
		} else {
			// TODO: Error - mismatch / user does not exist
		}
		if(matchingUser==null)
		{
			throw new RuntimeException("not found this user: "+userName);
		}
		return matchingUser;
	}
	public static void setQrLoginId(AuthenticationFlowContext context, String qrLoginId) {
		final String methodName = "setQrLoginId";
		SecurityVerifyLoggingUtilities.entry(logger, methodName, context, qrLoginId);

		context.getAuthenticationSession().setUserSessionNote("qr.login.id", qrLoginId);

		SecurityVerifyLoggingUtilities.exit(logger, methodName);
	}


	public static String getQrLoginImage(AuthenticationFlowContext context) {
		final String methodName = "getQrLoginImage";
		SecurityVerifyLoggingUtilities.entry(logger, methodName, context);

		String result = context.getAuthenticationSession().getUserSessionNotes().get("qr.login.image");

		SecurityVerifyLoggingUtilities.exit(logger, methodName, result);
		return result;
	}


}
