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

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.secsign.java.rest.*;
import org.jboss.logging.Logger;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.spi.HttpResponse;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.common.util.ServerCookie;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.PasswordCredentialProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;

import org.keycloak.models.utils.FormMessage;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.managers.AuthenticationManager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class QrCodeAuthenticator extends UsernamePasswordForm implements Authenticator {

	private static final Logger logger = Logger.getLogger(QrCodeAuthenticator.class);
    
    /**
     * called when the auth process is started
     * check for cookie, session or start secsign auth
     */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        String accessPassIcon="";
		MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
		String loginHint = context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);

		String rememberMeUsername = AuthenticationManager.getRememberMeUsername(context.getRealm(), context.getHttpRequest().getHttpHeaders());

		if (context.getUser() != null) {
			LoginFormsProvider form = context.form();
			form.setAttribute(LoginFormsProvider.USERNAME_HIDDEN, true);
			form.setAttribute(LoginFormsProvider.REGISTRATION_DISABLED, true);
			context.getAuthenticationSession().setAuthNote(USER_SET_BEFORE_USERNAME_PASSWORD_AUTH, "true");
		} else {
			context.getAuthenticationSession().removeAuthNote(USER_SET_BEFORE_USERNAME_PASSWORD_AUTH);
			if (loginHint != null || rememberMeUsername != null) {
				if (loginHint != null) {
					formData.add(AuthenticationManager.FORM_USERNAME, loginHint);
				} else {
					formData.add(AuthenticationManager.FORM_USERNAME, rememberMeUsername);
					formData.add("rememberMe", "on");
				}
			}
		}
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
		if(username==null)
		{

			String qrLoginId = QrUtilities.getQrLoginId(context);
			String qrLoginImage = QrUtilities.getQrLoginImage(context);
			QrLoginResponse qrResponse = Connector.
					pollQrLoginStatus(context, qrLoginId);
			Boolean isAuth =checkStateQrCode(qrResponse);
			while (isAuth==false){
				try {
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
				isAuth = checkStateQrCode( Connector.pollQrLoginStatus(context, qrLoginId));
			}
			user = QrUtilities.matchCIUserNameToUserModel(context, qrResponse.userName);
				if (user != null) {
					context.setUser(user);
					context.success();
				} else {
					context.form().setAttribute("accessPassIconData", QrUtilities.getQrLoginImage(context));
					Response challenge =  context.form()
							.setError("Username or Password not correct!")
							.createForm("secsign-accesspass.ftl");
					context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
				}
		}
		else {
			if (!validateForm(context, formData)) {
				context.form().setAttribute("accessPassIconData", QrUtilities.getQrLoginImage(context));
				Response challenge =  context.form()
						.setError("Username or Password not correct!")
						.createForm("secsign-accesspass.ftl");
				context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
			}
			context.success();
		}
    }
    /**
     * needs to give true, as we want to authenticate the user by the auth process and not by provided data
     */
	public void close() {
		// No-op
	}
	public Boolean checkStateQrCode(QrLoginResponse qrResponse){

		System.out.println(qrResponse.state);
		System.out.println(qrResponse.userName);
		System.out.println(qrResponse.userName != null);
		if ("SUCCESS".equals(qrResponse.state) && qrResponse.userName != null){
			return true;
		}
		return false;
	}
	public void addCookie(AuthenticationFlowContext context, String name, String value, String path, String domain, String comment, int maxAge, boolean secure, boolean httpOnly) {
		HttpResponse response = context.getSession().getContext().getContextObject(HttpResponse.class);
		StringBuffer cookieBuf = new StringBuffer();
		ServerCookie.appendCookieValue(cookieBuf, 1, name, value, path, domain, comment, maxAge, secure, httpOnly, null);
		String cookie = cookieBuf.toString();
		response.getOutputHeaders().add(HttpHeaders.SET_COOKIE, cookie);
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
