package nz.ac.auckland.concert.service.services;


import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import nz.ac.auckland.concert.service.domain.*;
import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.types.PriceBand;

/*
 * Class implementation of a REST Web Service for managing concerts
 * 
 * Implementation utilises JAX-RS as the choice of remote communication 
 * technology, with JAXB for Xml marshalling/unmarshalling and JPA for
 * persisting objects to relational database.
 * 
 */
@Path("/api")
@Produces({MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_XML})
public class ConcertResource {

	private static final long RESERVATION_EXPIRY_TIME_IN_SECONDS = 5;

	@GET
	@Path("/concerts")
	public Response getConcerts() {
		try {
			EntityManager em = PersistenceManager.instance().createEntityManager();
			em.getTransaction().begin();
			
			// Query database for all Concerts.
			TypedQuery<Concert> cq = em.createQuery("SELECT c FROM Concert c", Concert.class);
			List<Concert> concerts = cq.getResultList();
			
			// Check if there aren't any concerts.
			if (concerts.isEmpty()) {
				return Response.noContent().build();
			}
			
			// Convert Concerts to DTO Objects.
			Set<ConcertDTO> DTOConcerts = new HashSet<>();
			for (Concert c : concerts) {
				DTOConcerts.add(c.convertToDTO());
			}
			
			em.close();
			
			// Wrap DTO concerts in GenericEntity to preserve the generic type for MessageBodyWriter.
			GenericEntity<Set<ConcertDTO>> genericDTOConcerts = new GenericEntity<Set<ConcertDTO>>(DTOConcerts) {
			};
			
			// Initialise instance of cache control
			CacheControl cc = new CacheControl();
			// Set the max age of the cache control directive
			cc.setMaxAge(10);
			// Set cache control directive to true as it is included in the resposne
			cc.setPrivate(true);
			
			return Response.ok(genericDTOConcerts).cacheControl(cc).build();
			
		} catch(Exception e) {
			return Response.serverError().build();
		}
	}
	
	@GET
	@Path("/performers")
	public Response getPerformers() {
		try {
			EntityManager em = PersistenceManager.instance().createEntityManager();
			em.getTransaction().begin();
			
			// Query database for all Performers.
			TypedQuery<Performer> pq = em.createQuery("SELECT p FROM Performer p", Performer.class);
			List<Performer> performers = pq.getResultList();
			
			// Check if there aren't any performers.
			if (performers.isEmpty()) {
				return Response.noContent().build();
			}
			
			// Convert Performers to DTO Objects.
			Set<PerformerDTO> DTOPerformers = new HashSet<>();
			for (Performer p : performers) {
				DTOPerformers.add(p.convertToDTO());
			}
			
			em.close();
			
			// Wrap DTO performers in GenericEntity to preserve the generic type for MessageBodyWriter.
			GenericEntity<Set<PerformerDTO>> genericDTOPerformers = new GenericEntity<Set<PerformerDTO>>(DTOPerformers) {
			};
			
			// Initialise instance of cache control
			CacheControl cc = new CacheControl();
			// Set the max age of the cache control directive
			cc.setMaxAge(10);
			// Set cache control directive to true as it is included in the resposne
			cc.setPrivate(true);
			
			return Response.ok(genericDTOPerformers).cacheControl(cc).build();
			
		} catch(Exception e) {
			return Response.serverError().build();
		}
	}
	
	@POST
	@Path("/user")
	public Response createUser(UserDTO newUser) {
		
		try {
			// Check if user has all required fields.
			String firstname = newUser.getFirstname();
			String lastname = newUser.getLastname();
			String username = newUser.getUsername();
			String password = newUser.getPassword();
			if (firstname == null || firstname == "" || lastname == null || lastname == "" || username == null || username == "" || password == null || password == "") {
				return Response.status(Response.Status.LENGTH_REQUIRED).build();
			}
			
			EntityManager em = PersistenceManager.instance().createEntityManager();
			em.getTransaction().begin();
			
			try {
				// Check if new user doesn't already exist then create new user.
				if (em.find(User.class, newUser.getUsername()) != null) {
					return Response.status(Response.Status.CONFLICT).build();
				}
				User u = new User(newUser);
				UUID token = UUID.randomUUID();
				u.set_token(token.toString());
				u.set_tokenTimeStamp(LocalDateTime.now());
				em.persist(u);
				em.getTransaction().commit();

				// Wrap DTO reservation in GenericEntity to preserve the generic type for MessageBodyWriter.
				GenericEntity<UserDTO> genericDTOUser = new GenericEntity<UserDTO>(u.convertToDTO()) {
				};
				return Response.created(URI.create("/user/" + newUser.getUsername())).entity(genericDTOUser).cookie(new NewCookie("authToken", token.toString())).build();
			
			} catch(Exception e) {
				return Response.status(Response.Status.CONFLICT).build();
			} finally {
				em.close();
			}
		} catch(Exception e) {
			return Response.serverError().build();
		}
	}
	
	@POST
	@Path("/authenticate")
	public Response authenticateUser(UserDTO user) {
		try {
			// Check if user has all required fields.
			String username = user.getUsername();
			String password = user.getPassword();
			if (username == null || username == "" || password == null || password == "") {
				return Response.status(Response.Status.LENGTH_REQUIRED).build();
			}
			
			EntityManager em = PersistenceManager.instance().createEntityManager();
			em.getTransaction().begin();
			
			// Check username and password and create token if needed.
			try {
				User u = em.find(User.class, user.getUsername());
				if (u == null) {
					return Response.status(Response.Status.NOT_FOUND).build();
				}
				if (!u.get_password().equals(user.getPassword())) {
					return Response.status(Response.Status.UNAUTHORIZED).build();
				}
				if (u.get_token() == null) {
					UUID token = UUID.randomUUID();
					u.set_token(token.toString());
					u.set_tokenTimeStamp(LocalDateTime.now());
					em.merge(u);
					em.getTransaction().commit();
				}
				
				// Wrap DTO reservation in GenericEntity to preserve the generic type for MessageBodyWriter.
				GenericEntity<UserDTO> genericDTOUser = new GenericEntity<UserDTO>(u.convertToDTO()) {
				};
				return Response.accepted().entity(genericDTOUser).cookie(new NewCookie("authToken", u.get_token())).build();
			} finally {
				em.close();
			}
		} catch(Exception e) {
			return Response.serverError().build();
		}
	}
	
	@POST
	@Path("/registerCreditCard")
	public Response registerCreditCard(CreditCardDTO creditCard, @CookieParam("authToken") Cookie token) {
		try {
			// Check if authentication token field is not empty.
			if (token == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			
			EntityManager em = PersistenceManager.instance().createEntityManager();
			em.getTransaction().begin();
			
			try {
				// Retrieve user and check if authentication token matches.
				User u = (User) em.createQuery("SELECT u FROM User u WHERE u._token=:token").setParameter("token", token.getValue()).getSingleResult();
				if (u == null) {
					return Response.status(Response.Status.UNAUTHORIZED).build();
				}

				// Register creditCard details to user.
				u.set_creditCard(creditCard);
				em.persist(u);
				em.getTransaction().commit();
				
				// Wrap DTO reservation in GenericEntity to preserve the generic type for MessageBodyWriter.
				GenericEntity<CreditCardDTO> genericDTOCreditCard = new GenericEntity<CreditCardDTO>(creditCard) {
				};
				return Response.accepted().entity(genericDTOCreditCard).build();
			} finally {
				em.close();
			}
		} catch(Exception e) {
			return Response.serverError().build();
		}
	}	
	
	@POST
	@Path("/reserveSeats")
	public Response reservationRequest(ReservationRequestDTO reservationRequest, @CookieParam("authToken") Cookie token) {
		try {
			// Check if authentication token field is not empty.
			if (token == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}

			EntityManager em = PersistenceManager.instance().createEntityManager();
			em.getTransaction().begin();

			try {
				// Retrieve concert and check if dates match the reservation request.
				TypedQuery<Concert> cq = em.createQuery("SELECT c FROM Concert c WHERE c._cid=:cid", Concert.class)
						.setParameter("cid", reservationRequest.getConcertId());
				Concert concert = cq.getSingleResult();
				if (!concert.get_dates().contains(reservationRequest.getDate())) {
					return Response.status(Response.Status.BAD_REQUEST).build();
				}
				
				// Retrieve user and check if authentication token matches.
				User u = (User) em.createQuery("SELECT u FROM User u WHERE u._token=:token")
						.setParameter("token", token.getValue()).getSingleResult();
				if (u == null) {
					return Response.status(Response.Status.UNAUTHORIZED).build();
				}
				
				// Flag for checking if transaction was successful, or failed due to optimistic concurrency control.
				boolean txnSuccess;
				
				// Get the minimum and maximum seat number for requested price band range, as seats are modeled from 1 to 374.
				int min = 1;
				int max = 374;
				if (PriceBand.PriceBandA == reservationRequest.getSeatType()) {
					min = 1;
					max = Seat.TOTALPRICEBANDA;
				} else if (PriceBand.PriceBandB == reservationRequest.getSeatType()) {
					min = Seat.TOTALPRICEBANDA + 1;
					max = Seat.TOTALPRICEBANDA + Seat.TOTALPRICEBANDB;
				} else if (PriceBand.PriceBandC == reservationRequest.getSeatType()) {
					min = Seat.TOTALPRICEBANDA + Seat.TOTALPRICEBANDB + 1;
					max = Seat.TOTALPRICEBANDA + Seat.TOTALPRICEBANDB + Seat.TOTALPRICEBANDC;
				}

				// Update seat availability else if concert has no seats reserved in DB, than create all 374 available 
				// seat instances and persist them to the DB.
				txnSuccess = false;
				while (!txnSuccess) {
					try {
						// Query all seats related to the requested concert.
						TypedQuery<Seat> sq = em.createQuery("SELECT s FROM Seat s WHERE s._concert._cid=:cid AND s._datetime=:date", Seat.class)
								.setParameter("cid", concert.get_cid())
								.setParameter("date", reservationRequest.getDate())
								.setLockMode(LockModeType.OPTIMISTIC);
						List<Seat> seats = sq.getResultList();
						
						// If no seats in db, then persist new 374 available seats into db,
						// else persist now available seats into db.
						if (seats.isEmpty()) {
							for (int i = 1; i <= 374; i++) {
								Seat s = new Seat();
								s.set_status(Seat.SeatStatus.AVAILABLE);
								s.set_concert(concert);
								s.set_datetime(reservationRequest.getDate());
								s.set_timestamp(LocalDateTime.now());
								s.set_number(i);
								em.persist(s);
							}
						} else {
							for (Seat s : seats) {
								if (s.get_status().equals(Seat.SeatStatus.PENDING) && s.get_timestamp().isBefore(LocalDateTime.now().minusSeconds(RESERVATION_EXPIRY_TIME_IN_SECONDS))) {
									s.set_status(Seat.SeatStatus.AVAILABLE);
									em.merge(s);
									try {
										em.remove(s.get_reservation());
									} catch(Exception e) {}
								}
							}
						}
						txnSuccess = true;
					} catch (OptimisticLockException e) {
						txnSuccess = false;
					}
				}
				
				// 
				txnSuccess = false;
				ReservationDTO reservationDTO = new ReservationDTO();
				while (!txnSuccess) {
					try {
						// Retrieve all seats from DB.
						TypedQuery<Seat> seatQuery = em.createQuery("SELECT s FROM Seat s WHERE s._concert._cid=:cid AND s._datetime=:date", Seat.class)
								.setParameter("cid", concert.get_cid())
								.setParameter("date", reservationRequest.getDate())
								.setLockMode(LockModeType.OPTIMISTIC);
						List<Seat> seats = seatQuery.getResultList();
						
						// Add available seats to pending seats.
						List<Seat> pendingSeats = new ArrayList<>();
						for (Seat s : seats) {
							if ((s.get_status().equals(Seat.SeatStatus.AVAILABLE)) || (s.get_status().equals(Seat.SeatStatus.PENDING) && s.get_timestamp().isBefore(LocalDateTime.now().minusSeconds(RESERVATION_EXPIRY_TIME_IN_SECONDS)))) {
								if (s.get_number() >= min && s.get_number() <= max) {
									pendingSeats.add(s);
									s.set_status(Seat.SeatStatus.PENDING);
									s.set_timestamp(LocalDateTime.now());
									if (pendingSeats.size() == reservationRequest.getNumberOfSeats()) {
										break;
									}
								}
							}
						}
						
						// Check if enough available seats for reservation request.
						if (pendingSeats.size() < reservationRequest.getNumberOfSeats()) {
							return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
						}

						// Create new reservation.
						Reservation reservation = new Reservation();
						reservation.set_concert(concert);
						reservation.set_confirmed(false);
						reservation.set_dateTime(reservationRequest.getDate());
						reservation.set_priceBand(reservationRequest.getSeatType());
						reservation.set_user(u);
						reservation.set_rid(UUID.randomUUID().getLeastSignificantBits());

						// Let all the reserved seats refer to the new Reservation.
						Set<SeatDTO> DTOReservedSeats = new HashSet<>();
						for (Seat s : pendingSeats) {
							s.set_reservation(reservation);
							s.set_datetime(reservation.get_dateTime());
							DTOReservedSeats.add(s.convertToDTO());
							em.persist(s);
						}
						
						// Create ReservationDTO to be transferred.
						reservationDTO = new ReservationDTO(reservation.get_rid(), reservationRequest, DTOReservedSeats);

						// Persist the reservation to DB.
						em.persist(reservation);
						em.getTransaction().commit();

						txnSuccess = true;
					} catch (OptimisticLockException e) {
						txnSuccess = false;
					}
				}
				
				// Wrap DTO reservation in GenericEntity to preserve the generic type for MessageBodyWriter.
				GenericEntity<ReservationDTO> genericDTOReservation = new GenericEntity<ReservationDTO>(reservationDTO) {
				};
				
				return Response.ok(genericDTOReservation).build();
			} finally {
				em.close();
			}
		} catch (Exception e) {
			return Response.serverError().build();
		}
	}
	
	@POST
	@Path("/confirmSeats")
	public Response confirmReservation(ReservationDTO reservation, @CookieParam("authToken") Cookie token) {
		try {
			// Check if authentication token field is not empty.
			if (token == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}

			EntityManager em = PersistenceManager.instance().createEntityManager();
			em.getTransaction().begin();
			
			try {
				// Retrieve user and check if authentication token matches and has a registered credit card.
				User u = (User) em.createQuery("SELECT u FROM User u WHERE u._token=:token")
						.setParameter("token", token.getValue()).getSingleResult();
				if (u == null) {
					return Response.status(Response.Status.UNAUTHORIZED).build();
				}
				if (!u.existingCreditCard()) {
					return Response.status(Response.Status.PAYMENT_REQUIRED).build();
				}
				
				// Retrieve reservation from DB.
				TypedQuery<Reservation> rq = em.createQuery("SELECT r FROM Reservation r WHERE r._rid=:rid", Reservation.class)
						.setParameter("rid", reservation.getId());
				Reservation r = rq.getSingleResult();
				
				// Store set of seat numbers associated with the reservation.
				Set<Integer> seatNumbers = new HashSet<>();
				for (SeatDTO dto : reservation.getSeats()) {
					seatNumbers.add(new Seat(dto).get_number());
				}
				
				// Flag for checking if transaction was successful, or failed due to optimistic concurrency control.
				boolean txnSuccess = false;
				// Flag for checking if seats were booked successfully, else seat/s have expired.
				boolean confirmationSuccess = true;
				List<Seat> seats = new ArrayList<>();
				while (!txnSuccess) {
					try {
						// Retrieve seats associated with the specific concert and seat numbers associated with the reservation.
						TypedQuery<Seat> seatQuery = em.createQuery("SELECT s FROM Seat s WHERE s._concert._cid=:cid AND s._datetime=:datetime AND s._number IN (:seats)", Seat.class)
								.setParameter("cid", reservation.getReservationRequest().getConcertId())
								.setParameter("datetime", reservation.getReservationRequest().getDate())
								.setParameter("seats", seatNumbers)
								.setLockMode(LockModeType.OPTIMISTIC);
						seats = seatQuery.getResultList();
						
						// Check if seats have expired.
						for (Seat s : seats) {
							if (s.get_timestamp().isAfter(LocalDateTime.now().minusSeconds(RESERVATION_EXPIRY_TIME_IN_SECONDS)) &&
									s.get_reservation().get_rid().equals(r.get_rid())) {
								s.set_status(Seat.SeatStatus.BOOKED);
							} else {
								confirmationSuccess = false;
							}
						}
						txnSuccess = true;
					} catch(OptimisticLockException e) {
						txnSuccess = false;
					}
				}
				
				// If successfully booked seats that set reservation as confirmed, else make seats available again.
				if (confirmationSuccess) {
					for (Seat s : seats) {
						r.get_seats().add(s.get_number());
					}
					r.set_confirmed(true);
					em.getTransaction().commit();
					return Response.ok().build();
				} else {
					for (Seat s : seats) {
						try {
							if (s.get_reservation().get_rid().equals(r.get_rid())) {
								s.set_status(Seat.SeatStatus.AVAILABLE);
							}
						} catch (Exception e) {
						}
					}
					em.getTransaction().commit();
					return Response.status(Response.Status.REQUEST_TIMEOUT).build();
				}
				
			} finally {
				em.close();
			}
		} catch(Exception e) {
			return Response.serverError().build();
		}
	}
	
	@GET
	@Path("/bookings")
	public Response getBookings(@CookieParam("authToken") Cookie token) {
		try {
			// Check if authentication token field is not empty.
			if (token == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			
			EntityManager em = PersistenceManager.instance().createEntityManager();
			em.getTransaction().begin();
			
			// Retrieve user and check if authentication token matches.
			User u = (User) em.createQuery("SELECT u FROM User u WHERE u._token=:token")
					.setParameter("token", token.getValue()).getSingleResult();
			if (u == null) {
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}
			
			// Query database for all reservations.
			TypedQuery<Reservation> rq = em.createQuery("SELECT r FROM Reservation r WHERE r._user._token=:token", Reservation.class)
					.setParameter("token", token.getValue());
			List<Reservation> reservations = rq.getResultList();
			
			// Check if there aren't any reservations.
			if (reservations.isEmpty()) {
				return Response.noContent().build();
			}
			
			// Convert confirmed reservations (bookings) to BookingDTOs.
			Set<BookingDTO> DTOBookings = new HashSet<>();
			for (Reservation r : reservations) {
				if (r.get_confirmed()) {
					DTOBookings.add(r.convertToDTO());
				}
			}
			
			em.close();
			
			// Wrap DTO bookings in GenericEntity to preserve the generic type for MessageBodyWriter.
			GenericEntity<Set<BookingDTO>> genericDTOBookings = new GenericEntity<Set<BookingDTO>>(DTOBookings) {
			};
			
			return Response.ok(genericDTOBookings).build();
			
		} catch(Exception e) {
			return Response.serverError().build();
		}
	}
	
	
}
