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

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class QrCodeAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

    public static final String PROVIDER_ID = "Login with QrCode";
    private static final QrCodeAuthenticator SINGLETON = new QrCodeAuthenticator();

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED
    };
    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    /**
     * tells whether setRequiredActions is called in the Authenticator to allow the user to steup the auth (i.e. create a QrLogin)
     */
    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    /**
     * determines whether the authenticator is configurable
     */
    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName("QrLogin_ServerURL");
        property.setLabel("QrLogin Server URL");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("The full url of the Secsign ID Server to use. E.g. https://httpapi.secsign.com. Leave emtpy for the default Cloud Server");
        configProperties.add(property);
        
        
        ProviderConfigProperty property2;
        property2 = new ProviderConfigProperty();
        property2.setName("SecSign_PIN_ACCOUNT");
        property2.setLabel("Pin Account User");
        property2.setType(ProviderConfigProperty.STRING_TYPE);
        property2.setHelpText("The Pin Account User for the QrLogin Server. Leave empty for the default Cloud Server");
        configProperties.add(property2);
        
        ProviderConfigProperty property3;
        property3 = new ProviderConfigProperty();
        property3.setName("SecSign_PIN_PASSWORD");
        property3.setLabel("Pin Account Password");
        property3.setType(ProviderConfigProperty.STRING_TYPE);
        property3.setHelpText("The Pin Account Password for the QrLogin Server. Leave empty for the default Cloud Server");
        configProperties.add(property3);



    }


    @Override
    public String getHelpText() {
        return "MFA with QrLogin is performed.";
    }

    @Override
    public String getDisplayType() {
        return "QrLogin";
    }

    @Override
    public String getReferenceCategory() {
        return "QrLogin";
    }

    @Override
    public void init(Config.Scope config) {
    	
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {

    }


}
