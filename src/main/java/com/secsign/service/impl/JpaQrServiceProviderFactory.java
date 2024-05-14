package com.secsign.service.impl;

import com.secsign.service.QrService;
import com.secsign.service.QrServiceProviderFactory;
import org.keycloak.Config;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import javax.persistence.EntityManager;

public class JpaQrServiceProviderFactory implements QrServiceProviderFactory {
    public static final String PROVIDER_ID = "jpa-qr";

    @Override
    public QrService create(KeycloakSession session) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        return new JpaQrService(session, em);
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

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
