package com.secsign.rest.resource;

import com.google.auto.service.AutoService;
import com.secsign.model.QrModel;
import com.secsign.service.QrService;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.models.*;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/** */
@JBossLog
@AutoService(RealmResourceProviderFactory.class)
public class QrResourceProviderFactory implements RealmResourceProviderFactory {

  public static final String ID = "qr";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public void close() {}

  @Override
  public QrResourceProvider create(KeycloakSession session) {
    log.debug("QrResourceProviderFactory::create");
    return new QrResourceProvider(session);
  }

  @Override
  public void init(Config.Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {

//    log.debug("QrResourceProviderFactory::postInit");
//
//    factory.register(
//        (ProviderEvent event) -> {
//            if (event instanceof RealmModel.RealmRemovedEvent) {
//            log.debug("RealmRemovedEvent");
//            realmRemoved((RealmModel.RealmRemovedEvent) event);
//          } else if (event instanceof QrModel.QrCreationEvent) {
//            log.debug("QrCreationEvent");
//            qrCreation((QrModel.QrCreationEvent) event);
//          } else if (event instanceof QrModel.QrRemovedEvent) {
//            log.debug("QrRemovedEvent");
//            organizationRemoved((QrModel.QrRemovedEvent) event);
//          }
//        });
  }

//
//  private void addRole(String name, ClientModel client, RoleModel parent, boolean composite) {
//    if (client.getRole(name) == null) {
//      RoleModel role = client.addRole(name);
//      role.setDescription("${role_" + name + "}");
//      if (composite) parent.addCompositeRole(role);
//    }
//  }
//
//  private void realmRemoved(RealmModel.RealmRemovedEvent event) {
//    event
//        .getKeycloakSession()
//        .getProvider(QrService.class)
//            .removeQrs();
//  }
//
//  private void qrCreation(QrModel.QrCreationEvent event) {
//    QrModel qrModel = event.getQr();
//
//    // create default admin user
//    String adminUsername = getDefaultAdminUsername(org);
//    UserModel user =
//        event
//            .getKeycloakSession()
//            .users()
//            .addUser(event.getRealm(), KeycloakModelUtils.generateId(), adminUsername, true, false);
//    user.setEnabled(true);
//    // other defaults? email? emailVerified? attributes?
//    user.setEmail(String.format("%s@noreply.phasetwo.io", adminUsername)); // todo dynamic email?
//    user.setEmailVerified(true);
//    qrModel.(user);
//    for (String role : DEFAULT_ORG_ROLES) {
//      QrRoleModel roleModel = org.getRoleByName(role);
//      roleModel.grantRole(user);
//    }
//  }
//
//  private void organizationRemoved(QrModel.QrRemovedEvent event) {
//    // TODO anything else to do? does cascade take care of it?
//
//    // remove the idp associations for this org
//    QrModel org = event.getQr();
//    try {
//      org.getIdentityProvidersStream()
//          .forEach(
//              idp -> {
//                idp.getConfig().remove(Orgs.ORG_OWNER_CONFIG_KEY);
//              });
//    } catch (Exception e) {
//      log.warnf(
//          "Couldn't remove identity providers on organizationRemoved. Likely because this follows a realmRemoved event. %s",
//          e.getMessage());
//    }
//
//    // delete default admin user
//    try {
//      UserModel user =
//          event
//              .getKeycloakSession()
//              .users()
//              .getUserByUsername(
//                  event.getRealm(), getDefaultAdminUsername(event.getQr()));
//      if (user != null) {
//        boolean removed = event.getKeycloakSession().users().removeUser(event.getRealm(), user);
//        log.debugf(
//            "User removed on deletion of org %s? %b", event.getQr().getId(), removed);
//      } else {
//        log.warnf(
//            "Default org admin %s for org %s doesn't exist. Skipping deletion on org removal.",
//            getDefaultAdminUsername(event.getQr()), event.getQr().getId());
//      }
//    } catch (Exception e) {
//      log.warnf(
//          "Couldn't remove default org admin user on organizationRemoved. Likely because this follows a realmRemoved event. %s",
//          e.getMessage());
//    }
//  }
//
//  public static String getDefaultAdminUsername(QrModel org) {
//    return String.format("org-admin-%s", org.getId());
//  }
}
