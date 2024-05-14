package com.secsign.rest.resource;

import org.keycloak.models.KeycloakSession;

import javax.ws.rs.Path;

public class QrRestResource {
    private final KeycloakSession session;

    public QrRestResource(KeycloakSession session) {
        this.session = session;

    }

    @Path("qr")
    public QrResource getQrResource() {
        return new QrResource(session);
    }

}
