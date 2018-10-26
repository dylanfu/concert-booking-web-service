package nz.ac.auckland.concert.common.dto;

import java.time.LocalDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import nz.ac.auckland.concert.common.jaxb.LocalDateTimeAdapter;

/**
 * DTO class to represent news.
 * 
 * A NewsDTO describes a news in terms of:
 * _id        	the unique identifier for the news.
 * _timestamp 	the news creation date and times (represented as a 
 *              LocalDateTime instance).
 * _message   	the news message.   
 *
 */
@XmlRootElement(name = "NewsDTO")
@XmlAccessorType(XmlAccessType.FIELD)
public class NewsDTO {

	@XmlAttribute(name = "id")
	private Long _id;
	
	@XmlElement(name = "timestamp")
	@XmlJavaTypeAdapter(value = LocalDateTimeAdapter.class)
	private LocalDateTime _timestamp;
	
	@XmlElement(name = "message")
	private String message;
	
	public Long get_id() {
		return _id;
	}
	
	public void set_id(Long _id) {
		this._id = _id;
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		result = prime * result + ((_timestamp == null) ? 0 : _timestamp.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NewsDTO other = (NewsDTO) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		if (_timestamp == null) {
			if (other._timestamp != null)
				return false;
		} else if (!_timestamp.equals(other._timestamp))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}
	
}
