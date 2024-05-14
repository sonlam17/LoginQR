package com.secsign.service;

import com.secsign.model.QrModel;
import org.keycloak.provider.Provider;

public interface QrService extends Provider {
    QrModel createQr(String name);
    QrModel getQrById(String id);
    void removeQrs();
    boolean removeQr(String id);
}
