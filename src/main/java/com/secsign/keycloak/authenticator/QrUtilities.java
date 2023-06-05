package com.secsign.keycloak.authenticator;

import com.secsign.java.rest.SecurityVerifyLoggingUtilities;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;


import java.io.IOException;

import java.net.URISyntaxException;
import java.util.List;

public class QrUtilities {
	private static Logger logger = Logger.getLogger(QrUtilities.class);
	
	public static final String DEFAULT_SERVER = "http://183.91.3.60:8080/sca-0.2";

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
	public static UserModel matchCIUserNameToUserModel(AuthenticationFlowContext context, String userName) throws IOException, URISyntaxException {
		UserModel matchingUser = null;
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
