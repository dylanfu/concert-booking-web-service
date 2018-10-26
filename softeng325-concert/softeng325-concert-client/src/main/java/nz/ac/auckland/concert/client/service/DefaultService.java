package nz.ac.auckland.concert.client.service;

import java.awt.Image;
import javax.imageio.ImageIO;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.NewsDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.common.message.Messages;

/**
 * Implementation of ConcertService interface for the concert service. 
 * 
 * Class for the implementation of the REST Web service client for the
 * concert service. 
 * 
 * The service is defined using several DTO classes, data types and messages.
 * These are defined in the following packages: 
 * - nz.ac.auckland.concert.common.dto
 * - nz.ac.auckland.concert.common.types
 * - nz.ac.auckland.concert.common.message
 *
 */
public class DefaultService implements ConcertService, ConcertService.NewsListener{

	// AWS S3 access credentials for concert images.
    private static final String AWS_ACCESS_KEY_ID = "AKIAJOG7SJ36SFVZNJMQ";
    private static final String AWS_SECRET_ACCESS_KEY = "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";

    // Name of the S3 bucket that stores images.
	private static final String AWS_BUCKET = "concert2.aucklanduni.ac.nz";
	
	private static String WEB_SERVICE_URI = "http://localhost:10000/services/api";
	private Cookie authToken;
	private Set<ConcertDTO> _concertList;
	private Set<PerformerDTO> _performerList;
	
	@Override
	public Set<ConcertDTO> getConcerts() throws ServiceException {
		Client client = ClientBuilder.newClient();
		try {
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/concerts").request().accept(MediaType.APPLICATION_XML);
			Response response = builder.get();
			Set<ConcertDTO> concerts;
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				concerts = response.readEntity(new GenericType<Set<ConcertDTO>>() {
				});
				_concertList = concerts;
			} else if (response.getStatus() == Response.Status.NOT_MODIFIED.getStatusCode()) {
				return _concertList;
			} else {
				concerts = new HashSet<>();
			}
			client.close();
			return concerts;
		} catch (Exception e) {
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		} finally {
			client.close();
		}
	}
	
	@Override
	public Set<PerformerDTO> getPerformers() throws ServiceException {
		Client client = ClientBuilder.newClient();
		try {
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/performers").request().accept(MediaType.APPLICATION_XML);
			Response response = builder.get();
			Set<PerformerDTO> performers;
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				performers = response.readEntity(new GenericType<Set<PerformerDTO>>() {
				});
				_performerList = performers;
			} else if (response.getStatus() == Response.Status.NOT_MODIFIED.getStatusCode()) {
				return _performerList;
			} else {
				performers = new HashSet<>();
			}
			client.close();
			return performers;
		} catch (Exception e) {
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		} finally {
			client.close();
		}
	}
	
	
	@Override
	public UserDTO createUser(UserDTO newUser) throws ServiceException {
		Client client = ClientBuilder.newClient();
		try {
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/user").request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
			Response response = builder.post(Entity.entity(newUser, MediaType.APPLICATION_XML));
			int responseCode = response.getStatus();
			if (responseCode == Response.Status.CREATED.getStatusCode()) {
				authToken = (Cookie) response.getCookies().values().toArray()[0];
				return response.readEntity(new GenericType<UserDTO>() {});
			} else if (responseCode == Response.Status.CONFLICT.getStatusCode()) {
				throw new ServiceException(Messages.CREATE_USER_WITH_NON_UNIQUE_NAME);
			} else if (responseCode == Response.Status.LENGTH_REQUIRED.getStatusCode()) {
				throw new ServiceException(Messages.CREATE_USER_WITH_MISSING_FIELDS);
			} else {
				throw new ServiceException("HTTP STATUS CODE UNEXPECTED");
			}
		} catch (Exception e) {
			if (ServiceException.class.isInstance(e)) {
				throw e;
			} else {
				throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
			}
		} finally {
			client.close();
		}
	}
	
	@Override
	public UserDTO authenticateUser(UserDTO user) throws ServiceException {
		Client client = ClientBuilder.newClient();
		try {
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/authenticate").request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
			Response response = builder.post(Entity.entity(user, MediaType.APPLICATION_XML));
			int responseCode = response.getStatus();
			if (responseCode == Response.Status.ACCEPTED.getStatusCode()) {
				authToken = (Cookie)response.getCookies().values().toArray()[0];
				return response.readEntity(new GenericType<UserDTO>() {});
			} else if (responseCode == Response.Status.LENGTH_REQUIRED.getStatusCode()) {
				throw new ServiceException(Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS);
			} else if (responseCode == Response.Status.NOT_FOUND.getStatusCode()) {
				throw new ServiceException(Messages.AUTHENTICATE_NON_EXISTENT_USER);
			} else if (responseCode == Response.Status.UNAUTHORIZED.getStatusCode()) {
				throw new ServiceException(Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD);
			} else {
				throw new ServiceException("HTTP STATUS CODE NOT EXPECTED");
			}
		} catch (Exception e) {
			if (ServiceException.class.isInstance(e)) {
				throw e;
			} else {
				throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
			}
		} finally {
			client.close();
		}
	}
	
	@Override
	public Image getImageForPerformer(PerformerDTO performer) throws ServiceException {
		try {
			// Return local copy if already fetched.
			String imageName = performer.getImageName();
			try {
				File filepath = new File(imageName);
				return ImageIO.read(filepath);
			} catch(Exception e) {
				
			}
			
			// Create an AmazonS3 object that represents a connection with the
			// remote S3 service.
			BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
			        AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
			AmazonS3 s3 = AmazonS3ClientBuilder
			        .standard()
			        .withRegion(Regions.AP_SOUTHEAST_2)
			        .withCredentials(
			                new AWSStaticCredentialsProvider(awsCredentials))
			        .build();
			
			S3Object s3o = s3.getObject(AWS_BUCKET, imageName);
			S3ObjectInputStream s3ois = s3o.getObjectContent();
			File f = new File(imageName);
			FileOutputStream fos = new FileOutputStream(f);
			byte[] buffer = new byte[1024];
			int length = 0;
			while ((length = s3ois.read(buffer)) > 0) {
				fos.write(buffer, 0, length);
			}
			s3ois.close();
			fos.close();

			return ImageIO.read(f);
		} catch (AmazonServiceException e) {
			return null;
		} catch (SdkClientException e) {
			return null;
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		} 
	}
	
	@Override
	public ReservationDTO reserveSeats(ReservationRequestDTO reservationRequest) throws ServiceException {
		if (authToken == null) {
			throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
		}

		Client client = ClientBuilder.newClient();
		try {
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/reserveSeats")
					.request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
			Response response = builder.cookie("authToken", authToken.getValue())
					.post(Entity.entity(reservationRequest, MediaType.APPLICATION_XML));

			int responseCode = response.getStatus();
			if (responseCode == Response.Status.OK.getStatusCode()) {
				return response.readEntity(ReservationDTO.class);
			} else if (responseCode == Response.Status.NOT_FOUND.getStatusCode()) {
				throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
			} else if (responseCode == Response.Status.UNAUTHORIZED.getStatusCode()) {
				throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
			} else if (responseCode == Response.Status.BAD_REQUEST.getStatusCode()) {
				throw new ServiceException(Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE);
			} else if (responseCode == Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE.getStatusCode()) {
				throw new ServiceException(Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION);
			} else {
				throw new ServiceException("UNEXPECTED HTTP STATUS CODE");
			}
		} catch (Exception e) {
			if (ServiceException.class.isInstance(e)) {
				throw e;
			} else {
				throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
			}
		} finally {
			client.close();
		}
	}
	
	@Override
	public void confirmReservation(ReservationDTO reservation) throws ServiceException {
		if (authToken == null) {
			throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
		}

		Client client = ClientBuilder.newClient();
		try {
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/confirmSeats")
					.request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
			Response response = builder.cookie("authToken", authToken.getValue())
					.post(Entity.entity(reservation, MediaType.APPLICATION_XML));

			int responseCode = response.getStatus();
			if (responseCode == Response.Status.OK.getStatusCode()) {
				return;
			} else if (responseCode == Response.Status.NOT_FOUND.getStatusCode()) {
				throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
			} else if (responseCode == Response.Status.UNAUTHORIZED.getStatusCode()) {
				throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
			} else if (responseCode == Response.Status.PAYMENT_REQUIRED.getStatusCode()) {
				throw new ServiceException(Messages.CREDIT_CARD_NOT_REGISTERED);
			} else if (responseCode == Response.Status.REQUEST_TIMEOUT.getStatusCode()) {
				throw new ServiceException(Messages.EXPIRED_RESERVATION);
			} else {
				throw new ServiceException("UNEXPECTED HTTP STATUS CODE");
			}
		} catch (Exception e) {
			if (ServiceException.class.isInstance(e)) {
				throw e;
			} else {
				throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
			}
		} finally {
			client.close();
		}
	}
	
	@Override
	public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {
		if (authToken == null) {
			throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
		}

		Client client = ClientBuilder.newClient();
		try {
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/registerCreditCard")
					.request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
			Response response = builder.cookie("authToken", authToken.getValue())
					.post(Entity.entity(creditCard, MediaType.APPLICATION_XML));

			int responseCode = response.getStatus();
			if (responseCode == Response.Status.ACCEPTED.getStatusCode()) {
				return;
			} else if (responseCode == Response.Status.NOT_FOUND.getStatusCode()) {
				throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
			} else if (responseCode == Response.Status.UNAUTHORIZED.getStatusCode()) {
				throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
			} else {
				throw new ServiceException("UNEXPECTED HTTP STATUS CODE");
			}
		} catch (Exception e) {
			if (ServiceException.class.isInstance(e)) {
				throw e;
			} else {
				throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
			}
		} finally {
			client.close();
		}
	}
	
	@Override
	public Set<BookingDTO> getBookings() throws ServiceException {
		if (authToken == null) {
			throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
		}

		Client client = ClientBuilder.newClient();
		try {
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/bookings")
					.request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
			Response response = builder.cookie("authToken", authToken.getValue()).get();

			int responseCode = response.getStatus();
			if (responseCode == Response.Status.OK.getStatusCode()) {
				return response.readEntity(new GenericType<Set<BookingDTO>>() {
				});
			} else if (responseCode == Response.Status.NOT_FOUND.getStatusCode()) {
				throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
			} else if (responseCode == Response.Status.UNAUTHORIZED.getStatusCode()) {
				throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
			} else {
				throw new ServiceException("UNEXPECTED HTTP STATUS CODE");
			}
		} catch (Exception e) {
			if (ServiceException.class.isInstance(e)) {
				throw e;
			} else {
				throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
			}
		} finally {
			client.close();
		}
	}

	@Override
	public void subscribeToNews() throws ServiceException {
		if (authToken == null) {
			throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
		}
		Client client = ClientBuilder.newClient();
		try {
			Future future = client.target("http://localhost:10000/services/news" + "/subscribeToNotifications").request().cookie(authToken).async()
					.get(new InvocationCallback<NewsDTO>() {

						@Override
						public void completed(NewsDTO news) {
							newsReceived(news);
						}

						@Override
						public void failed(Throwable throwable) {
						}
					});
		} catch (Exception e) {
			if (ServiceException.class.isInstance(e)) {
				throw e;
			} else {
				throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
			}
		} finally {
			client.close();
		}
	}

	@Override
	public void unsubscribeToNews() throws ServiceException {
		if (authToken == null) {
			throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
		}
		Client client = ClientBuilder.newClient();
		try {
			Invocation.Builder builder = client.target("http://localhost:10000/services/news" + "/unsubscribeToNotifications")
					.request(MediaType.APPLICATION_XML);
			Response response = builder.cookie("authToken", authToken.getValue()).delete();
			
			int responseCode = response.getStatus();
			if (responseCode == Response.Status.OK.getStatusCode()) {
				return;
			} else if (responseCode == Response.Status.NOT_FOUND.getStatusCode()) {
				throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
			} else if (responseCode == Response.Status.UNAUTHORIZED.getStatusCode()) {
				throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
			} else {
				throw new ServiceException("UNEXPECTED HTTP STATUS CODE");
			}
		} catch (Exception e) {
			if (ServiceException.class.isInstance(e)) {
				throw e;
			} else {
				throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
			}
		} finally {
			client.close();
		}
	}

	@Override
	public void newsReceived(NewsDTO news) {
		Client client = ClientBuilder.newClient();
		final WebTarget target =
				client.target("http://localhost:10000/services" + "/news");
		target.request()
				.async()
				.get(new InvocationCallback<String>() {
					public void completed(String message) {
						target.request().async().get(this);
					}

					public void failed(Throwable t) {
					}
				});
	}
}
