package com.secsign.keycloak.authenticator;

import com.secsign.java.rest.SecSignRESTConnector;

public class SecSignUtils {
	
	public static final String DEFAULT_SERVER = "https://httpapi.secsign.com";
	private static final String KEYCLOAK_PIN_ACCOUNT = "KEYCLOAK_ADDON";
	private static final String KEYCLOAK_PIN_ACCOUNT_PASSWORD = "?22OhZeFFq(k6AX.fde00";
	private static String serverURL=null;
	private static String pinAccountUser=null;
	private static String pinAccountPassword=null;

	public static SecSignRESTConnector getRESTConnector() throws NullPointerException
    {
		SecSignRESTConnector connector=null;
		if(serverURL!=null)
		{
			if(pinAccountUser==null || pinAccountUser.equals(""))
			{
				connector = new SecSignRESTConnector(serverURL,KEYCLOAK_PIN_ACCOUNT,KEYCLOAK_PIN_ACCOUNT_PASSWORD);
			}else {
				connector = new SecSignRESTConnector(serverURL,pinAccountUser,pinAccountPassword);
			}
			
		}else {
			throw new NullPointerException("No serverURL on creating Connector");
		}
    	
    	return connector;
    }

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
	
}
