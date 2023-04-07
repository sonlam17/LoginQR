/*
    Copyright 2020 IBM
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
      http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.secsign.java.rest;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SecurityVerifyUtilities {
	
	public static final String CONFIG_TENANT_FQDN = "tenant.fqdn";
	public static final String CONFIG_CLIENT_ID = "client.id";
	public static final String CONFIG_CLIENT_SECRET = "client.secret";

	private static Logger logger = Logger.getLogger(SecurityVerifyUtilities.class);
	
	public static String getAccessToken(AuthenticationFlowContext context) {
		final String methodName = "getAccessToken";
		SecurityVerifyLoggingUtilities.entry(logger, methodName, context);
		String accessToken = null;

		String tenantHostname = getTenantHostname(context);
		CloseableHttpClient httpClient = null;
		try {
			AuthenticatorConfigModel authenticatorConfigModel = context.getAuthenticatorConfig();
			if (authenticatorConfigModel != null) {
				Map<String, String> authenticatorConfig = authenticatorConfigModel.getConfig();
				if (authenticatorConfig != null) {
					// Load tenant configuration
					String clientId = authenticatorConfig.get(SecurityVerifyUtilities.CONFIG_CLIENT_ID);
					String clientSecret = authenticatorConfig.get(SecurityVerifyUtilities.CONFIG_CLIENT_SECRET);

					// Request for the access token
					httpClient = HttpClients.createDefault();
					URI uri = new URIBuilder()
							.setScheme("https")
							.setHost(tenantHostname)
							.setPath("/v1.0/endpoint/default/token")
							.build();
					HttpPost post = new HttpPost(uri);
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("client_id", clientId));
					params.add(new BasicNameValuePair("client_secret", clientSecret));
					params.add(new BasicNameValuePair("grant_type", "client_credentials"));
					post.setEntity(new UrlEncodedFormEntity(params));
					CloseableHttpResponse response = httpClient.execute(post);
					int statusCode = response.getStatusLine().getStatusCode();
					String responseBody = EntityUtils.toString(response.getEntity());
					EntityUtils.consume(response.getEntity());
					if (statusCode == 200) {
						Pattern accessTokenExtraction = Pattern.compile("\"access_token\":\"([a-zA-Z0-9]+)\"");
						Matcher matcher = accessTokenExtraction.matcher(responseBody);
						if (matcher.find()) {
							accessToken = matcher.group(1);
						}
					} else {
		                SecurityVerifyLoggingUtilities.error(logger, methodName, String.format("%s: %s", statusCode, responseBody));
		            }
					response.close();
				}
			}
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

		if (accessToken == null) {
		    // TODO: immediately abort the flow
		    Response challengeResponse = Response.serverError().build();
            context.failure(AuthenticationFlowError.INVALID_CLIENT_CREDENTIALS, challengeResponse);
		}

		SecurityVerifyLoggingUtilities.exit(logger, methodName, accessToken);
		return accessToken;
	}

	public static String getTenantHostname(AuthenticationFlowContext context) {
		final String methodName = "getTenantHostname";
		SecurityVerifyLoggingUtilities.entry(logger, methodName, context);

		String tenantHostname = null;
		AuthenticatorConfigModel authenticatorConfigModel = context.getAuthenticatorConfig();
		if (authenticatorConfigModel != null) {
			Map<String, String> authenticatorConfig = authenticatorConfigModel.getConfig();
			if (authenticatorConfig != null) {
				// Load tenant configuration
				tenantHostname = authenticatorConfig.get(SecurityVerifyUtilities.CONFIG_TENANT_FQDN);
			}
		}

		SecurityVerifyLoggingUtilities.exit(logger, methodName, tenantHostname);
		return tenantHostname;
	}
	
	public static UserModel matchCIUserIdToUserModel(AuthenticationFlowContext context, String userId) {
		final String methodName = "matchCIUserIdToUserModel";
		SecurityVerifyLoggingUtilities.entry(logger, methodName, context, userId);
		
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

		SecurityVerifyLoggingUtilities.exit(logger, methodName, matchingUser != null ? matchingUser.toString() : null);
		return matchingUser;
	}
	
	public static String getCIUserId(AuthenticationFlowContext context, UserModel user) {
		final String methodName = "getCIUserId";
		SecurityVerifyLoggingUtilities.entry(logger, methodName, user);
        
        String tenantHostname = SecurityVerifyUtilities.getTenantHostname(context);
        String accessToken = SecurityVerifyUtilities.getAccessToken(context);
        String ciUserId = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.createDefault();
            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost(tenantHostname)
                    .setPath("/v2.0/Users")
                    .setParameter("fullText", user.getId())
                    .build();
            HttpGet getRequest = new HttpGet(uri);
            getRequest.addHeader("Authorization", "Bearer " + accessToken);
            getRequest.addHeader("Accept", "application/scim+json");
            CloseableHttpResponse response = httpClient.execute(getRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            EntityUtils.consume(response.getEntity());
            if (statusCode == 200) {
                Pattern idExtraction = Pattern.compile("\"id\":\"([A-Z0-9]+)\"");
                Matcher matcher = idExtraction.matcher(responseBody);
                if (matcher.find()) {
                    ciUserId = matcher.group(1);
                }
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

		SecurityVerifyLoggingUtilities.exit(logger, methodName, ciUserId);
		return ciUserId;
	}

	public static String getKCUserId(AuthenticationFlowContext context, String ciUserId) {
        final String methodName = "getKCUserId";
        SecurityVerifyLoggingUtilities.entry(logger, methodName, ciUserId);
        
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

        SecurityVerifyLoggingUtilities.exit(logger, methodName, kcUserId);
        return kcUserId;
    }

	public static boolean createCIShadowUser(AuthenticationFlowContext context, UserModel user) {
		final String methodName = "createCIShadowUser";
		SecurityVerifyLoggingUtilities.entry(logger, methodName, context, user);
		
		boolean result = false;
		String tenantHostname = SecurityVerifyUtilities.getTenantHostname(context);
		String accessToken = SecurityVerifyUtilities.getAccessToken(context);
		CloseableHttpClient httpClient = null;
		try {
			httpClient = HttpClients.createDefault();
			URI uri = new URIBuilder()
					.setScheme("https")
					.setHost(tenantHostname)
					.setPath("/v2.0/Users")
					.build();
			HttpPost postRequest = new HttpPost(uri);
			postRequest.addHeader("Authorization", "Bearer " + accessToken);
			postRequest.addHeader("Accept", "application/scim+json");
			postRequest.addHeader("Content-type", "application/scim+json");
			String createUserPayload = String.format(
				"{\"userName\": \"%s\",\"urn:ietf:params:scim:schemas:extension:ibm:2.0:Notification\": {\"notifyType\": \"NONE\"}, \"externalId\": \"%s\", \"emails\": [{\"type\": \"work\", \"value\": \"%s\"}], \"schemas\": [\"urn:ietf:params:scim:schemas:core:2.0:User\", \"urn:ietf:params:scim:schemas:extension:ibm:2.0:Notification\"]}",
				user.getUsername(),
				user.getId(),
				user.getEmail()
			);
			postRequest.setEntity(new StringEntity(createUserPayload));
			CloseableHttpResponse response = httpClient.execute(postRequest);
			int statusCode = response.getStatusLine().getStatusCode();
			String responseBody = EntityUtils.toString(response.getEntity());
			EntityUtils.consume(response.getEntity());
			if (statusCode == 201) {
				Pattern idExtraction = Pattern.compile("\"id\":\\s*\"(\\w+)\"");
				Matcher matcher = idExtraction.matcher(responseBody);
				if (matcher.find()) {
					String ciUserId = matcher.group(1);
					if (ciUserId != null) {
						//setCIUserId(user, ciUserId);
						result = true;
					}
				}
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
		
		SecurityVerifyLoggingUtilities.exit(logger, methodName, result);
		return result;
	}
	
	public static void setPromptedPasswordlessRegistration(AuthenticationFlowContext context) {
		final String methodName = "setPromptedPasswordlessRegistration";
		SecurityVerifyLoggingUtilities.entry(logger, methodName, context);
		
		context.getAuthenticationSession().setAuthNote("prompt.passwordless.registration", Boolean.TRUE.toString());
		
		SecurityVerifyLoggingUtilities.exit(logger, methodName);
	}
	
	public static void clearPromptedPasswordlessRegistration(AuthenticationFlowContext context) {
		final String methodName = "clearPromptedPasswordlessRegistration";
		SecurityVerifyLoggingUtilities.entry(logger, methodName, context);
		
		context.getAuthenticationSession().setAuthNote("prompt.passwordless.registration", Boolean.FALSE.toString());
		
		SecurityVerifyLoggingUtilities.exit(logger, methodName);
	}
	
	public static boolean hasPromptedPasswordlessRegistration(AuthenticationFlowContext context) {
		final String methodName = "hasPromptedPasswordlessRegistration";
		SecurityVerifyLoggingUtilities.entry(logger, methodName, context);
		
		String authNote = context.getAuthenticationSession().getAuthNote("prompt.passwordless.registration");
		boolean hasPrompted = authNote == null ? false : Boolean.valueOf(authNote);
		
		SecurityVerifyLoggingUtilities.exit(logger, methodName, hasPrompted);
		return hasPrompted;
	}
}
