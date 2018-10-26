package nz.ac.auckland.concert.service.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "NEWS")
public class News {
	
	@Id
	@GeneratedValue
	@Column(name = "nid", nullable = false)
	private Long _nid;
	
	@Column(name = "timestamp", nullable = false)
	private LocalDateTime _timestamp;
	
	@Column(name = "message", nullable = false)
	private String message;

	public Long get_nid() {
		return _nid;
	}

	public void set_nid(Long _nid) {
		this._nid = _nid;
	}

	public LocalDateTime get_timestamp() {
		return _timestamp;
	}

	public void set_timestamp(LocalDateTime _timestamp) {
		this._timestamp = _timestamp;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
