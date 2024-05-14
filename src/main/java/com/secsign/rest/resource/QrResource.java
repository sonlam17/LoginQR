package com.secsign.rest.resource;
import com.secsign.service.QrService;
import com.secsign.representation.QrRepresentation;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class QrResource {
    private final KeycloakSession session;

    public QrResource(KeycloakSession session) {
        this.session = session;
    }

//    @GET
//    @Path("")
//    @NoCache
//    @Produces(MediaType.APPLICATION_JSON)
//    public List<QrRepresentation> getCompanies() {
//        return session.getProvider(ExampleService.class).listCompanies();
//    }

    @POST
    @Path("")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createCompany(QrRepresentation rep) {
        session.getProvider(QrService.class).createQr(rep.getContent());
        return Response.created(session.getContext().getUri().getAbsolutePathBuilder().path(rep.getId()).build()).build();
    }
//
//    @GET
//    @NoCache
//    @Path("{id}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public CompanyRepresentation getCompany(@PathParam("id") final String id) {
//        return session.getProvider(ExampleService.class).findCompany(id);
//    }

}
