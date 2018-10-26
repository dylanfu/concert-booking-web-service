package nz.ac.auckland.concert.client.service;

/**
 * Unchecked exception subclass used to describe exceptions that occur when 
 * using the ConcertService interface.
 * 
 * A ServiceException contains a message that provides a description of the 
 * exception.
 *
 */
public class ServiceException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public ServiceException(String message) {
		super(message);
	}
}
