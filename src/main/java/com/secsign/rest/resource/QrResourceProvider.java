package com.secsign.rest.resource;

import com.secsign.representation.QrRepresentation;
import com.secsign.service.QrService;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/** */
@JBossLog
public class QrResourceProvider implements RealmResourceProvider {


  private KeycloakSession session;

  public QrResourceProvider(KeycloakSession session) {
    this.session = session;
  }

  @Override
  public Object getResource() {
    return this;
  }

  @Override
  public void close() {

  }
  @POST
  @Path("")
  @NoCache
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createCompany(QrRepresentation rep) {
    session.getProvider(QrService.class).createQr(rep.getContent());
    return Response.created(session.getContext().getUri().getAbsolutePathBuilder().path(rep.getId()).build()).build();
  }
}
