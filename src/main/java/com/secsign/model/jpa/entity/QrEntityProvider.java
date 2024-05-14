package com.secsign.model.jpa.entity;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;

import java.util.Collections;
import java.util.List;

public class QrEntityProvider implements JpaEntityProvider{
    @Override
    public List<Class<?>> getEntities() {
        return Collections.<Class<?>>singletonList(QrEntity.class);
    }

    @Override
    public String getChangelogLocation() {
        return "META-INF/qr-changelog.xml";
    }

    @Override
    public String getFactoryId() {
        return QrJpaEntityProviderFactory.ID;
    }

    @Override
    public void close() {

    }
}
