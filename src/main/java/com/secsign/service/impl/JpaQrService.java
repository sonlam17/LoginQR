package com.secsign.service.impl;

import com.secsign.model.QRAdapter;
import com.secsign.model.jpa.entity.QrEntity;
import com.secsign.model.QrModel;
import com.secsign.representation.QrRepresentation;
import com.secsign.service.QrService;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;

public class JpaQrService implements QrService {
    protected final KeycloakSession session;
    protected final EntityManager em;
    protected final RealmModel realm;
    public JpaQrService(KeycloakSession session, EntityManager em) {
        this.session = session;
        this.em = em;
        this.realm = session.getContext().getRealm();;
    }

    @Override
    public QrModel createQr(QrRepresentation qrRepresentation) {
        QrEntity e = new QrEntity();
        String id = KeycloakModelUtils.generateId();
        e.setId(id);
        e.setRealmId(realm.getId());
        e.setContent(qrRepresentation.getContent()+"realms/"+realm.getId()+"/qr"+ "/setState/"+ id);
        e.setState(qrRepresentation.getState());
        e.setUserId(null);

        em.persist(e);
        em.flush();
        QrModel qr = new QRAdapter(session, e, em, realm);
        session.getKeycloakSessionFactory().publish(qrCreationEvent(realm, qr));

        return qr;
    }

    @Override
    public QrModel getQrById( String id) {
        try {
            return getById(id);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public QrModel updateQrById(String id, String userId) {
        QrModel qrModel;
        QrEntity org = em.find(QrEntity.class, id);
        if (org != null && org.getRealmId().equals(realm.getId())) {
            qrModel = new QRAdapter(session, org, em, realm);
        } else {
            return null;
        }
        qrModel.setState(true);
        qrModel.setUserId(userId);
        return qrModel;
    }

    @Override
    public void removeQrs() {

    }

    @Override
    public boolean removeQr( String id) {
        return false;
    }

    @Override
    public void close() {

    }
    private QrModel getById(String id){
        Session sessionH = em.unwrap(Session.class);
        sessionH.beginTransaction();
// Lấy entity từ cơ sở dữ liệu hoặc từ quản lý
        Query<QrEntity> query = sessionH.createQuery("SELECT qr FROM QrEntity qr WHERE qr.id = :id", QrEntity.class);
        query.setParameter("id", id);
        QrEntity qr = query.getSingleResult();
        if (qr != null) {
            // Làm mới entity từ cơ sở dữ liệu
            sessionH.refresh(qr);
        }
        sessionH.evict(qr);
        sessionH.getTransaction().commit();
        if (qr != null && qr.getRealmId().equals(realm.getId())) {
            return new QRAdapter(session, qr, em, realm);
        } else {
            return null;
        }
    }
    public QrModel.QrCreationEvent qrCreationEvent(
            RealmModel realm, QrModel qr) {
        return new QrModel.QrCreationEvent() {

            @Override
            public QrModel getQr() {
                return qr;
            }
            @Override
            public KeycloakSession getKeycloakSession() {
                return session;
            }

            @Override
            public RealmModel getRealm() {
                return realm;
            }
        };
    }

    public QrModel.QrRemovedEvent orgRemovedEvent(
            RealmModel realm, QrModel qr) {
        return new QrModel.QrRemovedEvent() {
            @Override
            public QrModel getQr() {
                return qr;
            }

            @Override
            public KeycloakSession getKeycloakSession() {
                return session;
            }

            @Override
            public RealmModel getRealm() {
                return realm;
            }
        };
    }
}
