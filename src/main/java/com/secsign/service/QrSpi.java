package com.secsign.service;

import com.google.auto.service.AutoService;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;
@AutoService(Spi.class)
public class QrSpi implements Spi {

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getName() {
        return "organizationProvider";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return QrService.class;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return QrServiceProviderFactory.class;
    }
}
