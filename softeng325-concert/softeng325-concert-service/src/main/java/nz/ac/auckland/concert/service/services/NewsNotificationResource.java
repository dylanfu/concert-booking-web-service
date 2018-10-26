package nz.ac.auckland.concert.service.services;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nz.ac.auckland.concert.service.domain.User;

@Path("/news")
@Produces({MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_XML})
public class NewsNotificationResource {
	
	private Set<Cookie> _tokens = new LinkedHashSet<>();
	private Map<Cookie, AsyncResponse> _userResponses = new LinkedHashMap<>();

	@POST
	@Path("/subscribeToNotifications")
	public synchronized Response subscribeToNewsNotifications(@Suspended AsyncResponse response, @CookieParam("authToken") Cookie token) {
		try {
			_tokens.add(token);
			_userResponses.put(token, response);
			
			EntityManager em = PersistenceManager.instance().createEntityManager();
			em.getTransaction().begin();
			
			try {
				User u = (User) em.createQuery("SELECT U FROM User U WHERE U._token = :token")
						.setParameter("token", token.getValue())
						.getSingleResult();
				if (u == null) {
					return Response.status(Response.Status.UNAUTHORIZED).build();
				}
			} finally {
				em.close();
			}
			return Response.ok().build();
		} catch(Exception e) {
			_tokens.remove(token);
			_userResponses.remove(token);
			return Response.serverError().build();
		}
	}
	
	@DELETE
	@Path("/unsubscribeToNotifications")
	public synchronized Response unsubscribeToNewsNotifications(@CookieParam("authToken") Cookie token) {
		try {
			EntityManager em = PersistenceManager.instance().createEntityManager();
			em.getTransaction().begin();
			
			try {
				User u = (User) em.createQuery("SELECT U FROM User U WHERE U._token = :token")
						.setParameter("token", token.getValue())
						.getSingleResult();
				if (u == null) {
					return Response.status(Response.Status.UNAUTHORIZED).build();
				}
			} finally {
				em.close();
			}
			
			_tokens.remove(token);
			_userResponses.remove(token);
			
			return Response.ok().build();
		} catch(Exception e) {
			return Response.serverError().build();
		}
	}
	
	@POST
	public synchronized void send(String message) {
		for (AsyncResponse response : _userResponses.values()) {
			response.resume(message);
		}
	}
}
