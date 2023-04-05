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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import com.secsign.java.rest.SecSignIDRESTCheckAuthSessionStateResponse;
import com.secsign.java.rest.SecSignIDRESTCreateAuthSessionResponse;
import com.secsign.java.rest.SecSignIDRESTException;
import com.secsign.java.rest.SecSignIDRESTPluginRegistrationResponse;
import com.secsign.java.rest.SecSignRESTConnector;
import com.secsign.java.rest.SecSignRESTConnector.PluginType;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SecSignAuthenticator implements Authenticator {

	private static final Logger logger = Logger.getLogger(SecSignAuthenticator.class);

    
    private void initConnector(AuthenticationFlowContext context) throws NullPointerException, SecSignIDRESTException
    {
    	//get serverURL and save to Utils for later access
		AuthenticatorConfigModel authenticatorConfigModel = context.getAuthenticatorConfig();
		
		//check for config with custom serverURL
		if (authenticatorConfigModel != null && authenticatorConfigModel.getConfig().get("SecSign_ServerURL")!=null &&
				!authenticatorConfigModel.getConfig().get("SecSign_ServerURL").equals("")) 
    	{
			//not default server, get all data from config
			SecSignUtils.saveServerURL(authenticatorConfigModel.getConfig().get("SecSign_ServerURL"));
			String pinAccountUser=authenticatorConfigModel.getConfig().get("SecSign_PIN_ACCOUNT");
			String pinAccountPassword=authenticatorConfigModel.getConfig().get("SecSign_PIN_PASSWORD");
			SecSignUtils.setPinAccount(pinAccountUser, pinAccountPassword);
		}else {
			//defaultServer , get PinAccount user or create one, if none exists
			SecSignUtils.saveServerURL(SecSignUtils.DEFAULT_SERVER);
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
				SecSignUtils.setPinAccount(pinAccountUser, pinAccountPassword);
			}else {
				SecSignIDRESTPluginRegistrationResponse response;
				response = SecSignUtils.getRESTConnector().registerPlugin(context.getUriInfo().getBaseUri().toString(),"Keycloak Add-On at "+context.getUriInfo().getBaseUri().getHost(),"Keycloak Add-On",PluginType.CUSTOM);
				secsignUser.setSingleAttribute("pin_account_password", response.getPassword());
	    		secsignUser.setSingleAttribute("pin_account_user", response.getAccountName());
	    		SecSignUtils.setPinAccount(response.getAccountName(), response.getPassword());
				
	    		
			}
		}
    }
    
    /**
     * called when the auth process is started
     * check for cookie, session or start secsign auth
     */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
		try {
			initConnector(context);
		} catch (NullPointerException e1) {
			context.form().setAttribute("errorMsg", e1.getMessage());
        	context.form().setAttribute("tryAgainLink", context.getRefreshUrl(false));
			Response challenge = context.form()
	                .createForm("secsign-error.ftl");
	        context.challenge(challenge);
	        return;
		} catch (SecSignIDRESTException e1) {
			context.form().setAttribute("errorMsg", e1.getMessage());
        	context.form().setAttribute("tryAgainLink", context.getRefreshUrl(false));
			Response challenge = context.form()
	                .createForm("secsign-error.ftl");
	        context.challenge(challenge);
	        return;
		}
		
    	
        String accessPassIcon="";
        String secsignid="";
        String authSessionID="";
        //get saved session infos to show old authSession again (no double auth)
        if(context.getAuthenticationSession().getAuthNote("secsign_authsessionid")!=null)
        {
        	accessPassIcon=context.getAuthenticationSession().getAuthNote("secsign_authsessionicondata");
        	secsignid=context.getAuthenticationSession().getAuthNote("secsign_secsignid");
        	authSessionID=context.getAuthenticationSession().getAuthNote("secsign_authsessionid");
        	
        }else {
	        //no existing auth session, so start one
	        SecSignRESTConnector connector= SecSignUtils.getRESTConnector();
	        try {
	        	secsignid=context.getUser().getFirstAttribute("secsignid");
	        	
	        	SecSignIDRESTCreateAuthSessionResponse result= connector.getAuthSession(secsignid, context.getRealm().getDisplayName()+"@Keycloak",context.getUriInfo().getBaseUri().toString(), true);
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
		        	context.getAuthenticationSession().setAuthNote("secsign_authsessionid", String.valueOf(result.getAuthSessionId()));
		        	context.getAuthenticationSession().setAuthNote("secsign_authsessionicondata", String.valueOf(result.getAuthSessionIconData()));
		        	context.getAuthenticationSession().setAuthNote("secsign_secsignid", String.valueOf(result.getSecSignId()));
	        	}
	        } catch (NullPointerException e1) {
	        	context.form().setAttribute("errorMsg", e1.getMessage());
	        	context.form().setAttribute("tryAgainLink", context.getRefreshUrl(false));
				Response challenge = context.form()
		                .createForm("secsign-error.ftl");
		        context.challenge(challenge);
		        return;
			} catch (SecSignIDRESTException e1) {
				context.form().setAttribute("errorMsg", e1.getMessage());
	        	context.form().setAttribute("tryAgainLink", context.getRefreshUrl(false));
				Response challenge = context.form()
		                .createForm("secsign-error.ftl");
		        context.challenge(challenge);
		        return;
			}
			
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

    
   
    
    /**
     * method called when form is send
     * 1. start SecSign Auth
     * 2. Check AuthSession
     * 3. Login Done
     */
    @Override
    public void action(AuthenticationFlowContext context) {
        boolean authed=true;
    	logger.debug("action is called");

    	String errorMsg="";
    	//get authSessionID from Form
    	String authSessionID=context.getHttpRequest().getFormParameters().getFirst("secsign_authSessionID");
    	switch(context.getHttpRequest().getFormParameters().getFirst("secsign_accessPassAction"))
    	{
			authed=true;
    		case "checkAuth":
    		{
	    			try {
	    				SecSignRESTConnector connector=SecSignUtils.getRESTConnector();
////
	    	    	context.getAuthenticationSession().removeAuthNote("secsign_authsessionicondata");
	    	    	context.getAuthenticationSession().removeAuthNote("secsign_secsignid");
	    	    	context.getAuthenticationSession().removeAuthNote("secsign_authsessionid");

	    	        if(!authed)
	    	        {
	    	        	context.form().setAttribute("errorMsg", errorMsg);
	    	        	context.form().setAttribute("tryAgainLink", context.getRefreshUrl(false));
	    				Response challenge = context.form()
	    		                .createForm("secsign-error.ftl");
	    		        context.challenge(challenge);
	    		        return;
	    	        }else {
	    	        	//authed, so proceed login
	    		        //setCookie(context);
	    		        context.success();
	    	        }
    			break;
    		}
			}
    		case "cancelAuth":
    			try {
    				SecSignRESTConnector connector=SecSignUtils.getRESTConnector();
					connector.cancelAuthSession(authSessionID);
					context.resetFlow();
    			} catch (SecSignIDRESTException e1) {
    				context.form().setAttribute("errorMsg", e1.getMessage());
    	        	context.form().setAttribute("tryAgainLink", context.getRefreshUrl(false));
    				Response challenge = context.form()
    		                .createForm("secsign-error.ftl");
    		        context.challenge(challenge);
    		        return;
    			}

    			break;
    		default:
    			break;
    	}
    	
    	
    }

  
    

   



    /**
     * needs to give true, as we want to authenticate the user by the auth process and not by provided data
     */
    @Override
    public boolean requiresUser() {
        return true;
    }

    /**
     * determines whether the user is able to use this authenticator
     * check SecSign ID saved?
     */
    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
    	//check whether SecSign Id is saved for user, else he needs to create one
    	if(user.getFirstAttribute("secsignid")!=null)
    	{
    		return true;
    	}else {
    		return false;
    	}
    }

    /**
     * sets all actions that are required to allow the authentication for the user if configuredFor is false
     * e.g. creating a SecSign ID
     */
    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    	
    	//Add Create SecSign ID Action as required for not configured users
        user.addRequiredAction("CREATE_SECSIGNID_NEW");
        
    }

    public List<RequiredActionFactory> getRequiredActions(KeycloakSession session) {
        return Collections.singletonList((SecSignRequiredActionFactory)session.getKeycloakSessionFactory().getProviderFactory(RequiredActionProvider.class, SecSignRequiredAction.PROVIDER_ID));
    }

    @Override
    public void close() {

    }

    
}
