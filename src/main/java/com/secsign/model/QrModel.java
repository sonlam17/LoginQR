package com.secsign.model;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderEvent;

public interface QrModel {
    String getId();

    String getContent();

    void setContent(String url);

    String getState();

    void setState(String url);

    void removeQr();
    interface QrEvent extends ProviderEvent {
        QrModel getQr();

        KeycloakSession getKeycloakSession();

        RealmModel getRealm();
    }

    interface QrCreationEvent extends QrEvent {}

    interface QrRemovedEvent extends QrEvent {}
}
