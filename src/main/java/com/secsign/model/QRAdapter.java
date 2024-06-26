package com.secsign.model;
import com.secsign.model.jpa.entity.QrEntity;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.JpaModel;

import javax.persistence.EntityManager;

public class QRAdapter implements QrModel, JpaModel<QrEntity> {
    protected final KeycloakSession session;
    protected final QrEntity qr;
    protected final EntityManager em;
    protected final RealmModel realm;

    public QRAdapter(KeycloakSession session, QrEntity qr, EntityManager em, RealmModel realm) {
        this.session = session;
        this.qr = qr;
        this.em = em;
        this.realm = realm;
    }
    @Override
    public QrEntity getEntity() {
        return qr;
    }

    @Override
    public String getId() {
        return qr.getId();
    }

    @Override
    public String getContent() {
        return qr.getContent();
    }

    @Override
    public void setContent(String content) {
        qr.setContent(content);
    }

    @Override
    public String getState() {
        return qr.getState();
    }

    @Override
    public void setState(String state) {
        qr.setState(state);
    }

    @Override
    public void removeQr() {

    }
}
