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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.UserModel;

import com.secsign.java.rest.SecSignIDRESTCheckAuthSessionStateResponse;
import com.secsign.java.rest.SecSignIDRESTCreateAuthSessionResponse;
import com.secsign.java.rest.SecSignIDRESTCreateQRCodeResponse;
import com.secsign.java.rest.SecSignIDRESTException;
import com.secsign.java.rest.SecSignIDRESTPluginRegistrationResponse;
import com.secsign.java.rest.SecSignRESTConnector;
import com.secsign.java.rest.SecSignRESTConnector.PluginType;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SecSignRequiredAction implements RequiredActionProvider {
	private static final Logger logger = Logger.getLogger(SecSignRequiredAction.class);
    public static final String PROVIDER_ID = "CREATE_SECSIGNID_NEW";

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
    	UserModel user= context.getUser();
    	if(user.getFirstAttribute("secsignid")==null)
    	{
    		context.getUser().addRequiredAction(PROVIDER_ID);
    	}
    }
    
    private void initConnector(RequiredActionContext context) throws NullPointerException, SecSignIDRESTException
    {
    	
    	//search for authenticatorConfig with custom serverURL
    	Stream<AuthenticatorConfigModel> configsAsStream=context.getRealm().getAuthenticatorConfigsStream();
    	configsAsStream.forEach( authenticatorConfigModel -> {
    		if(authenticatorConfigModel.getConfig().get("SecSign_ServerURL")!=null &&
				!authenticatorConfigModel.getConfig().get("SecSign_ServerURL").equals(""))
			{
				//not default server, get all data from config
				SecSignUtils.saveServerURL(authenticatorConfigModel.getConfig().get("SecSign_ServerURL"));
				String pinAccountUser=authenticatorConfigModel.getConfig().get("SecSign_PIN_ACCOUNT");
				String pinAccountPassword=authenticatorConfigModel.getConfig().get("SecSign_PIN_PASSWORD");
				SecSignUtils.setPinAccount(pinAccountUser, pinAccountPassword);
			}
    	});
    	
			
    	if(!SecSignUtils.hasPinAccount())
    	{
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

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
    	UserModel user=context.getUser();
    	logger.debug("requiredActionChallenge called");
    	
    	try {
			initConnector(context);
		} catch (NullPointerException e1) {
			showError(e1.getMessage(), context);
			return;
		} catch (SecSignIDRESTException e1) {
			showError(e1.getMessage(), context);
			return;
		}
    	
    	String accessPassIcon="";
        String secsignid="";
        String authSessionID="";
        //get authSession infos from session to show old authSession
        if(context.getAuthenticationSession().getAuthNote("secsign_create_authsessionid")!=null)
        {
        	accessPassIcon=context.getAuthenticationSession().getAuthNote("secsign_create_authsessionicondata");
        	secsignid=context.getAuthenticationSession().getAuthNote("secsign_create_secsignid");
        	authSessionID=context.getAuthenticationSession().getAuthNote("secsign_create_authsessionid");
        	//set variables for ftl template
	        context.form().setAttribute("accessPassIconData", accessPassIcon);
	        context.form().setAttribute("secsignid", secsignid);
	        context.form().setAttribute("authSessionID", authSessionID);
	       
	        //hsow ftl template
	        Response challenge = context.form()
	                .createForm("secsign-accesspass.ftl");
	        
	        
	        context.challenge(challenge);
	        return;
        }else {
        	//get createQR infos from session to show old createQR
        	if(context.getAuthenticationSession().getAuthNote("secsign_create_createQRCode")!=null)
	        {
	        	String createQRCode=context.getAuthenticationSession().getAuthNote("secsign_create_createQRCode");
	        	String createURL=context.getAuthenticationSession().getAuthNote("secsign_create_createURL");
	        	String secSignID=context.getAuthenticationSession().getAuthNote("secsign_create_secsignid");
	        	context.form().setAttribute("createQRCode", createQRCode);
	    		context.form().setAttribute("createURL", createURL);
	    		context.form().setAttribute("secsignid", secSignID);
	    		Response challenge = context.form().createForm("secsign-create.ftl");
		        context.challenge(challenge);
		        return;
	        }else {
    	
	        	//no old session infos, create new secsignid
	        	try {
		        	SecSignRESTConnector connector=SecSignUtils.getRESTConnector();
			    	String secsignidToCreate=user.getUsername();
			    	String testID=secsignidToCreate;
			    	int i=0;
			    	//check which SecSign ID is available
					while(connector.checkSecSignID(testID))
					{
						i++;
						testID=secsignidToCreate+i;
						
					}
					secsignidToCreate=testID;
					//create SecSign ID
					SecSignIDRESTCreateQRCodeResponse answer=connector.getCreateSecSignIDQrCode(secsignidToCreate);
					//show SecSign ID QR Code
		    		context.form().setAttribute("createQRCode", answer.getQrCodeBase64());
		    		context.form().setAttribute("createURL", answer.getCreateUrl());
		    		context.form().setAttribute("secsignid", secsignidToCreate);
		    		context.getAuthenticationSession().setAuthNote("secsign_create_createQRCode", String.valueOf(answer.getQrCodeBase64()));
			        context.getAuthenticationSession().setAuthNote("secsign_create_createURL", String.valueOf(answer.getCreateUrl()));
			        context.getAuthenticationSession().setAuthNote("secsign_create_secsignid", String.valueOf(secsignidToCreate));
			        Response challenge = context.form().createForm("secsign-create.ftl");
			        context.challenge(challenge);
			        return;
	        	} catch (NullPointerException e1) {
	        		showError(e1.getMessage(), context);
	    	        return;
	    		} catch (SecSignIDRESTException e1) {
	    			showError(e1.getMessage(), context);
    				return;
	    		}
	        }
        }

    }

    @Override
    public void processAction(RequiredActionContext context) {
    	
    	if(context.getHttpRequest().getFormParameters().getFirst("secsign_createAction")!=null)
    	{
	    	switch(context.getHttpRequest().getFormParameters().getFirst("secsign_createAction"))
	    	{
	    	case "cancelCreation":
	    		//cancel Creation -> error, no login possible
	    		context.failure();
	    		return;
	    	case "checkCreation":
	    		//check whether ID is created yet
	    		String secSignID=URLDecoder.decode(context.getHttpRequest().getFormParameters().getFirst("secsign_secsignid"),StandardCharsets.UTF_8);
	    		try {
					boolean exists=SecSignUtils.getRESTConnector().checkSecSignID(secSignID);
					if(exists)
					{
						//Start auth for user to confirm creation
						//Delete qrCode session infos, to prevent showing up again after refresh
						context.getAuthenticationSession().removeAuthNote("secsign_create_createQRCode");
						context.getAuthenticationSession().removeAuthNote("secsign_create_createURL");
						context.getAuthenticationSession().removeAuthNote("secsign_create_secsignid");
						
				        
				        //create auth session
				        try {
				        	SecSignRESTConnector connector= SecSignUtils.getRESTConnector();
				        	SecSignIDRESTCreateAuthSessionResponse result= connector.getAuthSession(secSignID, context.getRealm().getDisplayName()+"@Keycloak",context.getUriInfo().getBaseUri().toString(), true);
				        	if(result.getFrozen())
				        	{
				        		showError("This SecSign ID is frozen due to concurrent login requests. The user needs to reactivate his account first. This can be done by tapping on the respective SecSign ID in the SecSign ID app.", context);
						        return;
				        	}else {
					        	String accessPassIcon=result.getAuthSessionIconData();
					        	String secsignid=result.getSecSignId();
					        	String authSessionID=String.valueOf(result.getAuthSessionId());
					        	
					        	//save in session to be able to show on refresh or leave page
					        	context.getAuthenticationSession().setAuthNote("secsign_create_authsessionid", String.valueOf(result.getAuthSessionId()));
					        	context.getAuthenticationSession().setAuthNote("secsign_create_authsessionicondata", String.valueOf(result.getAuthSessionIconData()));
					        	context.getAuthenticationSession().setAuthNote("secsign_create_secsignid", String.valueOf(result.getSecSignId()));
					        	 //set variables for ftl template
						        context.form().setAttribute("accessPassIconData", accessPassIcon);
						        context.form().setAttribute("secsignid", secsignid);
						        context.form().setAttribute("authSessionID", authSessionID);
						       
						        //hsow ftl template
						        Response challenge = context.form()
						                .createForm("secsign-accesspass.ftl");
						        
						        
						        context.challenge(challenge);
						        return;
				        	}
				        } catch (NullPointerException e1) {
				        	showError(e1.getMessage(), context);
		    				return;
						} catch (SecSignIDRESTException e1) {
							showError(e1.getMessage(), context);
		    				return;
						}
							
				      
					}else {
						// show qrCode again
						String createQRCode=URLDecoder.decode(context.getHttpRequest().getFormParameters().getFirst("secsign_createQRCode"),StandardCharsets.UTF_8);
						String createURL=URLDecoder.decode(context.getHttpRequest().getFormParameters().getFirst("secsign_createURL"),StandardCharsets.UTF_8);
						//reshow qr code
						context.form().setAttribute("createQRCode",createQRCode);
			    		context.form().setAttribute("createURL", createURL);
			    		context.form().setAttribute("secsignid", secSignID);
				
				        Response challenge = context.form().createForm("secsign-create.ftl");
				        context.challenge(challenge);
				        return;
					}
	    		} catch (NullPointerException e1) {
	    			showError(e1.getMessage(), context);
    				return;
				} catch (SecSignIDRESTException e1) {
					showError(e1.getMessage(), context);
    				return;
				}
	    	}
    	}else {
    		
    		 boolean authed=false;
	    	
	    	String errorMsg="";
	    	//get authSessionID from Form
	    	String authSessionID=context.getHttpRequest().getFormParameters().getFirst("secsign_authSessionID");
	    	switch(context.getHttpRequest().getFormParameters().getFirst("secsign_accessPassAction"))
	    	{
	    		case "checkAuth":
	    		{
		    			try {
		    				SecSignRESTConnector connector=SecSignUtils.getRESTConnector(); 
		    	    		SecSignIDRESTCheckAuthSessionStateResponse answer=connector.checkAuth(authSessionID);
		    	    		switch(answer.getAuthSessionState()){
		    				case AUTHENTICATED:
		    					//auth done
		    					authed=true;
		    					break;
		    				case CANCELED:
		    					errorMsg="Authentication Session canceled";
		    					authed=false;
		    					break;
		    				case INVALID:
		    					errorMsg="Authentication Session invalid";
		    					authed=false;
		    					break;
		    				case NO_STATE:
		    					errorMsg="Authentication Session has no state";
		    					authed=false;
		    					break;
		    				case SUSPENDED:
		    					errorMsg="Authentication Session suspended by the server";
		    					authed=false;
		    					break;
		    				case DENIED:
		    					errorMsg="Authentication Session denied by the user";
		    					authed=false;
		    					break;
		    				case EXPIRED:
		    					errorMsg="Authentication Session expired";
		    					authed=false;
		    					break;
		    				case PENDING:
		    				case FETCHED:
		    					//auth is in progress
		    						String accessPassIcon="";
		    				        String secsignid="";
		    				        authSessionID="";
		    			        
		    			        	accessPassIcon=context.getAuthenticationSession().getAuthNote("secsign_create_authsessionicondata");
		    			        	secsignid=context.getAuthenticationSession().getAuthNote("secsign_create_secsignid");
		    			        	authSessionID=context.getAuthenticationSession().getAuthNote("secsign_create_authsessionid");
		    			        	context.form().setAttribute("accessPassIconData", accessPassIcon);
		    				        context.form().setAttribute("secsignid", secsignid);
		    				        context.form().setAttribute("authSessionID", authSessionID);
		    				        Response challenge = context.form()
		    				                .createForm("secsign-accesspass.ftl");
		    				        
		    				        
		    				        context.challenge(challenge);
		    				        return;
		    				default:
		    					authed=false;
		    					break;
		    	    			
		    	    		}
		    			} catch (SecSignIDRESTException e1) {
		    				showError(e1.getMessage(), context);
		    				return;
		    			} catch (NullPointerException e1) {
		    				showError(e1.getMessage(), context);
		    				return;
		    			}
		    			
		    	    	//delete session information, else would be shown on next login again
		    	    	context.getAuthenticationSession().removeAuthNote("secsign_create_authsessionicondata");
		    	    	context.getAuthenticationSession().removeAuthNote("secsign_create_secsignid");
		    	    	context.getAuthenticationSession().removeAuthNote("secsign_create_authsessionid");
		    	    	
		    	        if(!authed)
		    	        {
		    	        	showError(errorMsg, context);
		    		        return;
		    	        }else {   
		    	        	//authed, so proceed login
		    		        //setCookie(context);
		    	        	context.getUser().removeRequiredAction(PROVIDER_ID);
		    	        	context.getUser().setSingleAttribute("secsignid", context.getHttpRequest().getFormParameters().getFirst("secsign_secsignid"));
		    		        context.success();
		    		        return;
		    	        }
	    		}
	    		case "cancelAuth":
	    			try {	
	    				SecSignRESTConnector connector=SecSignUtils.getRESTConnector(); 
						connector.cancelAuthSession(authSessionID);
						context.failure();
	    			} catch (SecSignIDRESTException e1) {
	    				showError(e1.getMessage(), context);
	    				return;
	    			}
	    				
	    			break;
	    		default:
	    			break;
	    	}
    	}
        context.failure();
        
    }
    
    private void showError(String errorMsg,RequiredActionContext context)
    {
    	context.form().setAttribute("errorMsg", errorMsg);
    	context.getAuthenticationSession().removeAuthNote("secsign_create_authsessionicondata");
    	context.getAuthenticationSession().removeAuthNote("secsign_create_secsignid");
    	context.getAuthenticationSession().removeAuthNote("secsign_create_authsessionid");
    	context.getAuthenticationSession().removeAuthNote("secsign_create_createQRCode");
		context.getAuthenticationSession().removeAuthNote("secsign_create_createURL");
		context.getAuthenticationSession().removeAuthNote("secsign_create_secsignid");
		Response challenge = context.form()
                .createForm("secsign-error.ftl");
        context.challenge(challenge);
    }

    @Override
    public void close() {

    }
}
