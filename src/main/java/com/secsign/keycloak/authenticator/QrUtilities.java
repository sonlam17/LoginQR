package com.secsign.keycloak.authenticator;

import com.secsign.rest.SecurityVerifyLoggingUtilities;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;


import java.util.List;

public class QrUtilities {
	private static Logger logger = Logger.getLogger(QrUtilities.class);
	
	public static final String DEFAULT_SERVER = "https://sec.cmcati.vn/iam-test-idp/";

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
	public static UserModel matchCIUserNameToUserModel(AuthenticationFlowContext context, String userId) {
		UserModel matchingUser = null;
		if (userId != null) {
			List<UserModel> users = context.getSession().users().getUsers(context.getRealm());
			UserModel iterUser;
			String userIdKeycloak;
			for (int i = 0; i < users.size(); i++) {
				iterUser = users.get(i);
				userIdKeycloak = iterUser.getId();
				if (userId.equals(userIdKeycloak)) {
					System.out.println(userIdKeycloak);
					matchingUser = iterUser;
					i = users.size();
				}
			}
		} else {
			System.out.println("not found user");
		}
		assert matchingUser != null;
		System.out.println("found user "+ matchingUser.getUsername());
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
