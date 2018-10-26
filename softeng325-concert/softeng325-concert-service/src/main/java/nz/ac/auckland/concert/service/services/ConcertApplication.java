package nz.ac.auckland.concert.service.services;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/*
 * Concert Web Service Application class for JAX-RS
 */
@ApplicationPath("/services")
public class ConcertApplication extends Application {
	
	private Set<Object> _singletons = new HashSet<>();
	private Set<Class<?>> _classes = new HashSet<>();

	public ConcertApplication() {
		_classes.add(ConcertResource.class);
		_singletons.add(new PersistenceManager());
		_singletons.add(new NewsNotificationResource());
	}

	@Override
	public Set<Object> getSingletons() { return _singletons;}

	@Override
	public Set<Class<?>> getClasses() {return _classes;}
}
