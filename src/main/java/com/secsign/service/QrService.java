package com.secsign.service;

import com.secsign.model.QrModel;
import com.secsign.representation.QrRepresentation;
import org.keycloak.provider.Provider;

public interface QrService extends Provider {
    QrModel createQr(QrRepresentation qrRepresentation);
    QrModel getQrById(String id);
    QrModel updateQrById(String id, String userId);
    void removeQrs();
    boolean removeQr(String id);
}
