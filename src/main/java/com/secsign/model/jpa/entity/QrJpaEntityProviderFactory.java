package com.secsign.model.jpa.entity;

import com.google.auto.service.AutoService;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
@JBossLog
@AutoService(JpaEntityProviderFactory.class)
public class QrJpaEntityProviderFactory implements JpaEntityProviderFactory {
    protected static final String ID = "qr-entity-provider";

    @Override
    public JpaEntityProvider create(KeycloakSession session) {
        log.debug("OrganizationEntityProviderFactory::create");
        return new QrEntityProvider();
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
        return ID;
    }
}
