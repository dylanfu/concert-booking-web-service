package nz.ac.auckland.concert.service.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.*;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.types.PriceBand;

@Entity
@Table(name = "CONCERTS")
public class Concert {
	
	@Id
    @GeneratedValue
	@Column(name = "cid", nullable = false, unique = true)
	private Long _cid;
	
	@Column(name = "title", nullable = false)
	private String _title;
	
	@ElementCollection
    @CollectionTable(name = "CONCERT_DATES", joinColumns =@JoinColumn(name = "cid"))
	@Column(name = "datetime", nullable = false, unique = true)
	private Set<LocalDateTime> _dates;
	
	@ElementCollection
	@JoinTable(name = "CONCERT_TARIFS", joinColumns = @JoinColumn(name = "cid"))
	@MapKeyColumn(name = "price_band")
    @Column(name = "tariff", nullable = false)
    @MapKeyEnumerated(EnumType.STRING)
	private Map<PriceBand, BigDecimal> _tariff;
	
	@ManyToMany
	@JoinTable(name = "CONCERT_PERFORMER", joinColumns = @JoinColumn(name = "cid"), inverseJoinColumns = @JoinColumn(name = "pid"))
	@Column(name = "performer", nullable = false, unique = true)
	private Set<Performer> _performers;

	public Concert() {}

	public ConcertDTO convertToDTO() {
		Set<Long> performerIds = new HashSet<>();
		for (Performer p: _performers) {
			performerIds.add(p.get_pid());
		}
		return new ConcertDTO(_cid, _title, _dates, _tariff, performerIds);
	}

	public Long get_cid() {
		return _cid;
	}

	public void set_cid(Long _cid) {
		this._cid = _cid;
	}

	public String get_title() {
		return _title;
	}

	public void set_title(String _title) {
		this._title = _title;
	}

	public Set<LocalDateTime> get_dates() {
		return _dates;
	}

	public void set_dates(Set<LocalDateTime> _dates) {
		this._dates = _dates;
	}

	public Map<PriceBand, BigDecimal> get_tariff() {
		return _tariff;
	}

	public void set_tariff(Map<PriceBand, BigDecimal> _tariff) {
		this._tariff = _tariff;
	}

	public Set<Performer> get_performers() {
		return _performers;
	}

	public void set_performers(Set<Performer> _performers) {
		this._performers = _performers;
	}
}
