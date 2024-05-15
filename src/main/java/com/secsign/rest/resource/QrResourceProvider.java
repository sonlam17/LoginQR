package com.secsign.rest.resource;

import com.secsign.representation.QrRepresentation;
import com.secsign.service.QrService;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.ForbiddenException;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resource.RealmResourceProvider;

import javax.validation.Valid;
import javax.ws.rs.*;
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
  public Response createQr(QrRepresentation rep) {
    AuthenticationManager.AuthResult authResult = checkAuth();
    session.getProvider(QrService.class).createQr(rep);
    return Response.created(session.getContext().getUri().getAbsolutePathBuilder().path(rep.getId()).build()).build();
  }
  @PUT
  @Path("setState/{qrId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateOrg(@PathParam("qrId") String qrId, @Valid QrRepresentation body) {
    AuthenticationManager.AuthResult authResult = checkAuth();
    String userId = authResult.getUser().getId();
    session.getProvider(QrService.class).updateQrById(body, qrId, userId);
    return Response.noContent().build();
  }
  private AuthenticationManager.AuthResult checkAuth(){
    AuthenticationManager.AuthResult auth = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
    if(auth ==null){
      throw new NotAuthorizedException("Bearer");
    } else if (auth.getToken().getIssuedFor()==null||!auth.getToken().getIssuedFor().equals("admin-cli")) {
      throw new ForbiddenException("Token is not properly issued for admin-cli");
    }
    return auth;
  }

}
