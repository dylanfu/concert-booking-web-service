package nz.ac.auckland.concert.service.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.types.PriceBand;

@Entity
@Table(name = "RESERVATIONS")
public class Reservation {
	
	@Id
	@Column(name = "rid", nullable = false)
	private Long _rid;

	@ManyToOne
	@JoinColumn(name = "user", nullable = false)
	private User _user;

	@ManyToOne
	@JoinColumn(name = "concert", nullable = false)
	private Concert _concert;

	@Column(name = "datetime")
	private LocalDateTime _dateTime;

	@Column(name = "priceband", nullable = false)
	@Enumerated(EnumType.STRING)
	private PriceBand _priceBand;

	@ElementCollection
	@CollectionTable(name = "RESERVATION_SEATS", joinColumns = @JoinColumn(name = "rid"))
	@Column(name = "seat", nullable = false)
	private Set<Integer> _seats;

	@Column(name = "confirmed", nullable = false)
	private Boolean _confirmed = false;

	public Reservation() {
	}

	public BookingDTO convertToDTO() {
		if (_confirmed) {
			Set<SeatDTO> sDTOs = new HashSet<>();
			for (Integer i : _seats) {
				sDTOs.add(new Seat(i).convertToDTO());
			}
			return new BookingDTO(_concert.get_cid(), _concert.get_title(), _dateTime, sDTOs, _priceBand);
		} else {
			return null;
		}
	}

	public Long get_rid() {
		return _rid;
	}

	public void set_rid(Long _rid) {
		this._rid = _rid;
	}

	public User get_user() {
		return _user;
	}

	public void set_user(User _user) {
		this._user = _user;
	}

	public Concert get_concert() {
		return _concert;
	}

	public void set_concert(Concert _concert) {
		this._concert = _concert;
	}

	public LocalDateTime get_dateTime() {
		return _dateTime;
	}

	public void set_dateTime(LocalDateTime _dateTime) {
		this._dateTime = _dateTime;
	}

	public PriceBand get_priceBand() {
		return _priceBand;
	}

	public void set_priceBand(PriceBand _priceBand) {
		this._priceBand = _priceBand;
	}

	public Set<Integer> get_seats() {
		return _seats;
	}

	public void set_seats(Set<Integer> _seats) {
		this._seats = _seats;
	}

	public Boolean get_confirmed() {
		return _confirmed;
	}

	public void set_confirmed(Boolean _confirmed) {
		this._confirmed = _confirmed;
	}

	@Override
	public int hashCode() {
		return _rid.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Reservation)) return false;

		Reservation that = (Reservation) o;

		return _rid.equals(that._rid);
	}
}
