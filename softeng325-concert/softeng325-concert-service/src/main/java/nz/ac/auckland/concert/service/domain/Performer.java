package nz.ac.auckland.concert.service.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.types.Genre;

@Entity
@Table(name = "PERFORMERS")
public class Performer {
	
	@Id
	@GeneratedValue
	@Column(name = "pid", nullable = false)
	private Long _pid;
	
	@Column(name = "name", nullable = false)
	private String _name;
	
	@Column(name = "imageName", nullable = false)
	private String _imageName;
	
	@Column(name = "genre", nullable = false)
    @Enumerated(EnumType.STRING)
	private Genre _genre;
	
	@ManyToMany
    @JoinTable(name = "CONCERT_PERFORMER", joinColumns = @JoinColumn(name = "pid"), inverseJoinColumns = @JoinColumn(name = "cid"))
    @Column(name = "concert", nullable = false)
	private Set<Concert> _concerts;

	public PerformerDTO convertToDTO() {
		Set<Long> concertIDs = new HashSet<>();
		for (Concert c : _concerts) {
			concertIDs.add(c.get_cid());
		}
		return new PerformerDTO(_pid, _name, _imageName, _genre, concertIDs);
	}
	
	public Long get_pid() {
		return _pid;
	}

	public void set_pid(Long _pid) {
		this._pid = _pid;
	}

	public String get_name() {
		return _name;
	}

	public void set_name(String _name) {
		this._name = _name;
	}

	public String get_imageName() {
		return _imageName;
	}

	public void set_imageName(String _imageName) {
		this._imageName = _imageName;
	}

	public Genre get_genre() {
		return _genre;
	}

	public void set_genre(Genre _genre) {
		this._genre = _genre;
	}

	public Set<Concert> get_concerts() {
		return _concerts;
	}

	public void set_concerts(Set<Concert> _concerts) {
		this._concerts = _concerts;
	}
	
}
