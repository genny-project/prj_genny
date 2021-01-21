package life.genny.endpoints;

import java.io.IOException;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.http.client.ClientProtocolException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.security.identity.SecurityIdentity;
import life.genny.models.GennyToken;

//import org.graalvm.nativeimage.Feature;
//import org.graalvm.nativeimage.RuntimeReflection;


//@AutomaticFeature
//@RegisterForReflection(targets = {
//		org.kie.internal.runtime.error.ExecutionError.class,
//		org.kie.internal.task.api.TaskVariable.class,
//		org.kie.internal.task.api.model.TaskEvent.class,
//		org.kie.internal.task.api.AuditTask.class,
//
//})


@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GennyResource {

	private static final Logger log = Logger.getLogger(GennyResource.class);

	@ConfigProperty(name = "default.realm", defaultValue = "genny")
	String defaultRealm;

	@Inject
	SecurityIdentity securityIdentity;

	@Inject
	JsonWebToken accessToken;

	@Inject
	UserTransaction userTransaction;

	@OPTIONS
	public Response opt() {
		return Response.ok().build();
	}

	@GET
	@Path("/test")
	public Response importAttributes(@Context UriInfo uriInfo, @QueryParam("url") String externalGennyUrl) {
		GennyToken userToken = new GennyToken(accessToken.getRawToken());
		if (userToken == null) {
			return Response.status(Status.FORBIDDEN).build();
		}

		if (!userToken.hasRole("dev") && !userToken.hasRole("superadmin")) {
			throw new WebApplicationException("User not recognised. Entity not being created", Status.FORBIDDEN);
		}

		log.info("External Genny Url = " + externalGennyUrl);
		try {
			userTransaction.setTransactionTimeout(3600);
			userTransaction.begin();

	
			userTransaction.commit();
			log.info("Finished import");
			return Response.status(Status.OK).build();

		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RollbackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HeuristicMixedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HeuristicRollbackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Response.status(Status.BAD_REQUEST).build();

	}



	@Transactional
	void onStart(@Observes StartupEvent ev) {
		log.info("Genny Endpoint starting");

	}

	@Transactional
	void onShutdown(@Observes ShutdownEvent ev) {
		log.info("Genny Endpoint Shutting down");
	}

}