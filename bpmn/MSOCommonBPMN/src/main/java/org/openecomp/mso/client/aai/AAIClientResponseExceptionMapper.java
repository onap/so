package org.openecomp.mso.client.aai;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.openecomp.mso.client.aai.entities.AAIError;

import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
@Priority(value = 1)
public class AAIClientResponseExceptionMapper implements ClientResponseFilter {

	@Override
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
		if (responseContext.getStatus() != Response.Status.OK.getStatusCode() && responseContext.hasEntity()) {
			AAIError error = new ObjectMapper().readValue(responseContext.getEntityStream(), AAIError.class);
			String message = error.getRequestError().getServiceException().getText();

			Response.Status status = Response.Status.fromStatusCode(responseContext.getStatus());
			WebApplicationException webAppException;
			switch (status) {
			case BAD_REQUEST:
				webAppException = new BadRequestException(message);
				break;
			case UNAUTHORIZED:
				webAppException = new NotAuthorizedException(message);
				break;
			case FORBIDDEN:
				webAppException = new ForbiddenException(message);
				break;
			case NOT_FOUND:
				webAppException = new NotFoundException(message);
				break;
			case METHOD_NOT_ALLOWED:
				webAppException = new NotAllowedException(message);
				break;
			case NOT_ACCEPTABLE:
				webAppException = new NotAcceptableException(message);
				break;
			case UNSUPPORTED_MEDIA_TYPE:
				webAppException = new NotSupportedException(message);
				break;
			case INTERNAL_SERVER_ERROR:
				webAppException = new InternalServerErrorException(message);
				break;
			case SERVICE_UNAVAILABLE:
				webAppException = new WebApplicationException(message);
				break;
			default:
				webAppException = new WebApplicationException(message);
			}
			throw webAppException;
		}
	}
}
