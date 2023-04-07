/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.secsign.keycloak.authenticator;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.secsign.java.rest.*;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import org.keycloak.models.utils.FormMessage;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SecSignAuthenticator implements Authenticator {

	private static final Logger logger = Logger.getLogger(SecSignAuthenticator.class);

    
    private void initConnector(AuthenticationFlowContext context) throws NullPointerException, Exception
    {
    	//get serverURL and save to Utils for later access
		AuthenticatorConfigModel authenticatorConfigModel = context.getAuthenticatorConfig();

		//check for config with custom serverURL
		if (authenticatorConfigModel != null && authenticatorConfigModel.getConfig().get("SecSign_ServerURL")!=null &&
				!authenticatorConfigModel.getConfig().get("SecSign_ServerURL").equals(""))
    	{
			//not default server, get all data from config
			QrUtilities.saveServerURL(authenticatorConfigModel.getConfig().get("SecSign_ServerURL"));
			String pinAccountUser=authenticatorConfigModel.getConfig().get("SecSign_PIN_ACCOUNT");
			String pinAccountPassword=authenticatorConfigModel.getConfig().get("SecSign_PIN_PASSWORD");
			QrUtilities.setPinAccount(pinAccountUser, pinAccountPassword);
		}else {
			//defaultServer , get PinAccount user or create one, if none exists
			QrUtilities.saveServerURL(QrUtilities.DEFAULT_SERVER);
			UserModel secsignUser=null;
	    	if(context.getSession().userLocalStorage().getUserByUsername(context.getRealm(), "SecSign_PinAccount")!=null)
	    	{
	    		//get pinAccount user
	    		secsignUser=context.getSession().userLocalStorage().getUserByUsername(context.getRealm(), "SecSign_PinAccount");
	    	}else {
	    		//create pinAccount user
	    		secsignUser=context.getSession().userLocalStorage().addUser(context.getRealm(), "SecSign_PinAccount");
	    	}

	    	//get Attributes from the user to get pinAccount data
			Map<String, List<String>> attributesForSecSignUser=secsignUser.getAttributes();
			if(attributesForSecSignUser.containsKey("pin_account_password"))
			{

				String pinAccountPassword=attributesForSecSignUser.get("pin_account_password").get(0);
				String pinAccountUser=attributesForSecSignUser.get("pin_account_user").get(0);
				QrUtilities.setPinAccount(pinAccountUser, pinAccountPassword);
			}
			//				SecSignIDRESTPluginRegistrationResponse response;
			//				response = SecSignUtils.getRESTConnector().registerPlugin(context.getUriInfo().getBaseUri().toString(),"Keycloak Add-On at "+context.getUriInfo().getBaseUri().getHost(),"Keycloak Add-On",PluginType.CUSTOM);
			//				secsignUser.setSingleAttribute("pin_account_password", response.getPassword());
			//	    		secsignUser.setSingleAttribute("pin_account_user", response.getAccountName());
			//	    		SecSignUtils.setPinAccount(response.getAccountName(), response.getPassword());
			//

		}
    }
    
    /**
     * called when the auth process is started
     * check for cookie, session or start secsign auth
     */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        String accessPassIcon="";
        String secsignid="";
        String authSessionID="";
	        //no existing auth session, so start one
	        Connector connector= new Connector(QrUtilities.DEFAULT_SERVER);
	        try {

	        	CreateAuthSessionResponse result= connector.getAuthSession(context.getRealm().getDisplayName()+"@Keycloak",context.getUriInfo().getBaseUri().toString(), true);
	        	if(result.getFrozen())
	        	{
	        		context.form().setAttribute("errorMsg", "This SecSign ID is frozen due to concurrent login requests. The user needs to reactivate his account first. This can be done by tapping on the respective SecSign ID in the SecSign ID app.");
		        	context.form().setAttribute("tryAgainLink", context.getRefreshUrl(false));
					Response challenge = context.form()
			                .createForm("secsign-error.ftl");
			        context.challenge(challenge);
			        return;
	        	}else {
		        	accessPassIcon=result.getAuthSessionIconData();
		        	secsignid=result.getSecSignId();
		        	authSessionID=String.valueOf(result.getAuthSessionId());
		        	
		        	//save in session to be able to show on refresh or leave page
		        	}
	        } catch (NullPointerException e1) {
	        	context.form().setAttribute("errorMsg", e1.getMessage());
	        	context.form().setAttribute("tryAgainLink", context.getRefreshUrl(false));
				Response challenge = context.form()
		                .createForm("secsign-error.ftl");
		        context.challenge(challenge);
		        return;
			}
				catch (Exception e1) {
				context.form().setAttribute("errorMsg", e1.getMessage());
	        	context.form().setAttribute("tryAgainLink", context.getRefreshUrl(false));
				Response challenge = context.form()
		                .createForm("secsign-error.ftl");
		        context.challenge(challenge);
		        return;
			}
			

        //set variables for ftl template
        context.form().setAttribute("accessPassIconData", accessPassIcon);
        context.form().setAttribute("secsignid", secsignid);
        context.form().setAttribute("authSessionID", authSessionID);
       
        //show ftl template
        Response challenge = context.form()
                .createForm("secsign-accesspass.ftl");
        
        
        context.challenge(challenge);
    
        
    }
	private static final String QR_CODE_ATTR_NAME = "qrCode";

	private static final String ACTION_PARAM = "action";
	private static final String VERIFY_REG_BTN = "hideBtn";
	private static final String AUTHENTICATE_PARAM = "authenticate";
	private static final String REGISTER_ACTION = "register";

	private static final String VERIFY_HIDE_REG_BUTTON = "verifyHideRegButton";

   
    
    /**
     * method called when form is send
     * 1. start SecSign Auth
     * 2. Check AuthSession
     * 3. Login Done
     */
    @Override
    public void action(AuthenticationFlowContext context) {
//		boolean authed=true;
//
//		String errorMsg="";
//		//get authSessionID from Form
//
//		context.getAuthenticationSession().removeAuthNote("secsign_create_authsessionicondata");
//		context.getAuthenticationSession().removeAuthNote("secsign_create_secsignid");
//		context.getAuthenticationSession().removeAuthNote("secsign_create_authsessionid");
//
//		if (!authed) {
//			context.form().setAttribute("errorMsg", errorMsg);
//			context.form().setAttribute("tryAgainLink", context.getRefreshUrl(false));
//			Response challenge = context.form()
//					.createForm("secsign-error.ftl");
//			context.challenge(challenge);
//
//		} else {
//			//authed, so proceed login
//			//setCookie(context);
//			context.success();
//		}
//
//
		MultivaluedMap<String, String> formParams = context.
				getHttpRequest().getDecodedFormParameters();
		String action= formParams.getFirst(ACTION_PARAM);
		String verifyRegVerified = formParams.getFirst(VERIFY_REG_BTN);

		if (REGISTER_ACTION.equals(action)) {
			// Redirect user to IBM Verify Registration (or next flow)
			context.attempted();
			return;
		}

		// Poll for the QR login
		String qrLoginId = QrUtilities.getQrLoginId(context);
		String qrLoginDsi = QrUtilities.getQrLoginDsi(context);
		String qrLoginImage = QrUtilities.getQrLoginImage(context);
		QrLoginResponse qrResponse = QrUtilities.
				pollQrLoginStatus(context, qrLoginId, qrLoginDsi);

		if (AUTHENTICATE_PARAM.equals(action) &&
				"SUCCESS".equals(qrResponse.state) && qrResponse.userId != null) {
			UserModel user = QrUtilities.matchCIUserIdToUserModel(context, qrResponse.userId);
			if (user != null) {
				context.setUser(user);
				context.success();
			} else {
				context.forceChallenge(FormUtilities.createErrorPage(context, new FormMessage("errorMsgUserDoesNotExist")));
				return;
			}
		} else if (AUTHENTICATE_PARAM.equals(action) && "FAILED".equals(qrResponse.state)) {
			// Attempted but authentication failed (not registered with IBM Verify)
			Response challenge = context.form()
					.setAttribute(QR_CODE_ATTR_NAME, qrLoginImage)
					.addError(new FormMessage("qrVerifyRegistrationRequiredError"))
					.createForm("secsign-accesspass.ftl");
			context.challenge(challenge);
		} else if (AUTHENTICATE_PARAM.equals(action) && "TIMEOUT".equals(qrResponse.state)) {
			context.form().addError(new FormMessage("qrFormLoginTimeOutError"));
			logger.log(Logger.Level.INFO,context);
			authenticate(context);
		} else if (AUTHENTICATE_PARAM.equals(action) && "PENDING".equals(qrResponse.state)) {
			Response challenge = context.form()
					.setAttribute(QR_CODE_ATTR_NAME, qrLoginImage)
					.setAttribute(VERIFY_HIDE_REG_BUTTON, Boolean.parseBoolean(verifyRegVerified))
					.createForm("secsign-accesspass.ftl");
			context.challenge(challenge);
		} else {
			// CANCELED
			context.forceChallenge(FormUtilities.createErrorPage(context, new FormMessage("errorMsgLoginCanceled")));
			return;
		}
    }

  
    

   



    /**
     * needs to give true, as we want to authenticate the user by the auth process and not by provided data
     */
	public void close() {
		// No-op
	}

	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		// Hardcode to true for the time being
		// Only users with verify configured should use this authenticator
		return true;
	}

	public boolean requiresUser() {
		// Doesn't require a user because the user will not yet have been authenticated
		return false;
	}

	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
		// No-op for the time being
	}



}
