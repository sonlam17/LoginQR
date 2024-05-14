package com.secsign.service;

import com.secsign.model.QrModel;
import com.secsign.representation.QrRepresentation;
import org.keycloak.provider.Provider;

public interface QrService extends Provider {
    QrModel createQr(QrRepresentation qrRepresentation);
    QrModel getQrById(String id);
    void removeQrs();
    boolean removeQr(String id);
}
