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

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.secsign.java.rest.*;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.PasswordCredentialProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;

import org.keycloak.models.utils.FormMessage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class QrCodeAuthenticator implements Authenticator {

	private static final Logger logger = Logger.getLogger(QrCodeAuthenticator.class);

    
    /**
     * called when the auth process is started
     * check for cookie, session or start secsign auth
     */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        String accessPassIcon="";

	        //no existing auth session, so start one
	        Connector connector= new Connector(QrUtilities.DEFAULT_SERVER);
	        try {

	        	CreateAuthSessionResponse result= connector.getAuthSession(context);
	        	if(result.getFrozen())
	        	{
	        		context.form().setAttribute("errorMsg", "This QrLogin is frozen due to concurrent login requests. The user needs to reactivate his account first. This can be done by tapping on the respective QrLogin in the QrLogin app.");
		        	context.form().setAttribute("tryAgainLink", context.getRefreshUrl(false));
					Response challenge = context.form()
			                .createForm("secsign-error.ftl");
			        context.challenge(challenge);
			        return;
	        	}else {
		        	accessPassIcon=result.getAuthSessionIconData();
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
        //show ftl template
        Response challenge = context.form()
                .createForm("secsign-accesspass.ftl");
        context.challenge(challenge);
    }
	private static final String QR_CODE_ATTR_NAME = "qrCode";

    /**
     * method called when form is send
     * 1. start QrLogin Auth
     * 2. Check AuthSession
     * 3. Login Done
     */
    @Override
    public void action(AuthenticationFlowContext context) {
		// Poll for the QR login
		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
		String username = formData.getFirst("username");
		UserModel user = null;
		if(username.isEmpty())
		{
			String qrLoginId = QrUtilities.getQrLoginId(context);
			String qrLoginImage = QrUtilities.getQrLoginImage(context);
			QrLoginResponse qrResponse = Connector.
					pollQrLoginStatus(context, qrLoginId);
			if ("SUCCESS".equals(qrResponse.state) && qrResponse.userName != null) {

				System.out.println("ở đây");
				try {
					System.out.println(qrResponse.userName);
					user = QrUtilities.matchCIUserNameToUserModel(context, qrResponse.userName);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
				if (user != null) {
					context.setUser(user);
					context.success();
				} else {
					context.forceChallenge(FormUtilities.createErrorPage(context, new FormMessage("errorMsgUserDoesNotExist")));
					return;
				}
			} else if ("FAILED".equals(qrResponse.state)) {
				// Attempted but authentication failed (not registered with IBM Verify)
				Response challenge = context.form()
						.setAttribute(QR_CODE_ATTR_NAME, qrLoginImage)
						.addError(new FormMessage("qrVerifyRegistrationRequiredError"))
						.createForm("secsign-accesspass.ftl");
				context.challenge(challenge);
			} else if ("TIMEOUT".equals(qrResponse.state)) {
				logger.log(Logger.Level.INFO,context);
				authenticate(context);
			} else if ("PENDING".equals(qrResponse.state)) {
				try {
					TimeUnit.SECONDS.sleep(3);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
				context.form().setAttribute("accessPassIconData", qrLoginImage);
				//show ftl template
				Response challenge = context.form()
						.createForm("secsign-accesspass.ftl");
				context.challenge(challenge);
			} else {
				context.forceChallenge(FormUtilities.createErrorPage(context, new FormMessage("errorMsgLoginCanceled")));
			}
		}
		else {
			try {
				user = QrUtilities.matchCIUserNameToUserModel(context, username);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
			context.setUser(user);
			System.out.println("OK...................");
			System.out.println(validateAnswer(context));
			if (validateAnswer(context)){
				context.success();
			}
			else {
				context.form().setAttribute("accessPassIconData", QrUtilities.getQrLoginImage(context));
				Response challenge =  context.form()
						.setError("Username or Password not correct!")
						.createForm("secsign-accesspass.ftl");
				context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
			}
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
	protected boolean validateAnswer(AuthenticationFlowContext context) {
		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
		String secret = formData.getFirst("password");
		String credentialId = formData.getFirst("credentialId");
		PasswordCredentialProvider passwordCredentialProvider =new PasswordCredentialProvider(context.getSession());
		if (credentialId == null || credentialId.isEmpty()) {
			credentialId = passwordCredentialProvider
					.getDefaultCredential(context.getSession(), context.getRealm(), context.getUser()).getId();
		}
		UserCredentialModel input = new UserCredentialModel(credentialId, "password", secret);
		return passwordCredentialProvider.isValid(context.getRealm(), context.getUser(), input);
	}
}
