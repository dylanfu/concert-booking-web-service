package nz.ac.auckland.concert.service.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.*;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;

import static nz.ac.auckland.concert.common.types.SeatRow.*;

@Entity
@IdClass(SeatId.class)
@Table(name = "SEATS")
public class Seat {
	
	@Version
	private int _version;
	
	@Column(name = "datetime", nullable = false)
	private LocalDateTime _datetime;
	
	@Column(name = "timestamp", nullable = true)
	private LocalDateTime _timestamp;
	
	@Id
	@ManyToOne
	private Concert _concert;
	
	@Id
	@Column(name = "number", nullable = false)
	private Integer _number;
	
	@ManyToOne
	private Reservation _reservation;
	
	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private SeatStatus _status = SeatStatus.AVAILABLE;
	
	public static final int TOTALPRICEBANDA = 167;
	public static final int TOTALPRICEBANDB = 81;
	public static final int TOTALPRICEBANDC = 126;
	
	public Seat() {}
	
	public Seat(Integer i) {
		_number = i.intValue();
	}
	
	public Seat(SeatDTO dto) {
		SeatRow row = dto.getRow();
		int seatNumber = dto.getNumber().intValue();
		_number = 0;		
		if (row.equals(A)) {
			_number = seatNumber + TOTALPRICEBANDA;
		} else if (row.equals(B)) {
			_number = seatNumber + TOTALPRICEBANDA + 19;
		} else if (row.equals(C)) {
			_number = seatNumber + TOTALPRICEBANDA + 39;
		} else if (row.equals(D)) {
			_number = seatNumber + TOTALPRICEBANDA + 60;
		} else if (row.equals(E)) {
			_number = seatNumber;
		} else if (row.equals(F)) {
			_number = seatNumber + 21;
		} else if (row.equals(G)) {
			_number = seatNumber + 43;
		} else if (row.equals(H)) {
			_number = seatNumber + TOTALPRICEBANDA + TOTALPRICEBANDB;
		} else if (row.equals(J)) {
			_number = seatNumber + 66;
		} else if (row.equals(K)) {
			_number = seatNumber + 91;
		} else if (row.equals(L)) {
			_number = seatNumber + 116;
		} else if (row.equals(M)) {
			_number = seatNumber + 141;
		} else if (row.equals(N)) {
			_number = seatNumber + TOTALPRICEBANDA + TOTALPRICEBANDB + 22;
		} else if (row.equals(O)) {
			_number = seatNumber + TOTALPRICEBANDA + TOTALPRICEBANDB + 48;
		} else if (row.equals(P)) {
			_number = seatNumber + TOTALPRICEBANDA + TOTALPRICEBANDB + 74;
		} else if (row.equals(R)) {
			_number = seatNumber + TOTALPRICEBANDA + TOTALPRICEBANDB + 100;
		}
		
	}
	
	public SeatDTO convertToDTO() {
		int count = _number;
		int[] seatsInRows;
		SeatRow[] rows;
		if (_number <= TOTALPRICEBANDA) {
			seatsInRows = new int[]{21, 22, 23, 25, 25, 25, 26};
			rows = new SeatRow[]{E, F, G, J, K, L, M};
		} else if (_number <= (TOTALPRICEBANDB + TOTALPRICEBANDA)) {
			seatsInRows = new int[]{19, 20, 21, 21};
			rows = new SeatRow[]{A, B, C, D};
			count -= TOTALPRICEBANDA;
		} else {
			seatsInRows = new int[]{22, 26, 26, 26, 26};
			rows = new SeatRow[]{H, N, O, P, R};
			count -= (TOTALPRICEBANDB + TOTALPRICEBANDA);
		}

		SeatDTO dto = new SeatDTO();
		for (int i = 0; i < rows.length; i++) {
			if (count > seatsInRows[i]) {
				count -= seatsInRows[i];
			} else {
				dto = new SeatDTO(rows[i], new SeatNumber(count));
				break;
			}
		}
		return dto;
	}
	
	public int get_version() {
		return _version;
	}

	public void set_version(int _version) {
		this._version = _version;
	}

	public LocalDateTime get_timestamp() {
		return _timestamp;
	}

	public void set_timestamp(LocalDateTime _timestamp) {
		this._timestamp = _timestamp;
	}

	public LocalDateTime get_datetime() {
		return _datetime;
	}

	public void set_datetime(LocalDateTime _datetime) {
		this._datetime = _datetime;
	}

	public Concert get_concert() {
		return _concert;
	}

	public void set_concert(Concert _concert) {
		this._concert = _concert;
	}

	public Integer get_number() {
		return _number;
	}

	public void set_number(Integer _number) {
		this._number = _number;
	}

	public Reservation get_reservation() {
		return _reservation;
	}

	public void set_reservation(Reservation _reservation) {
		this._reservation = _reservation;
		
	}

	public SeatStatus get_status() {
		return _status;
	}

	public void set_status(SeatStatus _status) {
		this._status = _status;
	}
	
	public enum SeatStatus {
		BOOKED, PENDING, AVAILABLE
	}
}

class SeatId implements Serializable {
	private static final long serialVersionUID = 1L;
	private Concert _concert;
	private LocalDateTime _datetime;
	private Integer _number;
	
	public Concert get_concert() {
		return _concert;
	}

	public void set_concert(Concert _concert) {
		this._concert = _concert;
	}

	public LocalDateTime get_datetime() {
		return _datetime;
	}

	public void set_datetime(LocalDateTime _datetime) {
		this._datetime = _datetime;
	}

	public Integer get_number() {
		return _number;
	}

	public void set_number(Integer _number) {
		this._number = _number;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
	            append(_concert).
	            append(_datetime).
	            append(_number).
	            hashCode();
	}

	@Override
	public boolean equals(Object obj) {		
		if (!(obj instanceof Seat))
            return false;
        if (obj == this)
            return true;

        SeatId rhs = (SeatId) obj;
        return new EqualsBuilder().
            append(_concert, rhs.get_concert()).
            append(_datetime, rhs.get_datetime()).
            append(_number, rhs.get_number()).
            isEquals();
	}
}

