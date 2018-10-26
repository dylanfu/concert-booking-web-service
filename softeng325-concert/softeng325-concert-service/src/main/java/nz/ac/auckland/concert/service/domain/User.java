package nz.ac.auckland.concert.service.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.*;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;

@Entity
@Table(name="USERS")
public class User {

    @Id
	@Column(name = "username", nullable = false, unique = true)
	private String _username;

    @Column(name = "password", nullable = false)
    private String _password;  

	@Column(name = "number", nullable = true, unique = true)
	private String _number;

    @Column(name = "type", nullable = true)
    @Enumerated(EnumType.STRING)
    private CreditCardDTO.Type _type;

    @Column(name = "name", nullable = true)
    private String _name;

    @Column(name = "date", nullable = true)
    private LocalDate _expiryDate;
    

    @Column(name = "firstname", nullable = false)
    private String _firstname;

    @Column(name = "lastname", nullable = false)
    private String _lastname;

	@Column(name = "token", nullable = true)
	private String _token;

	@Column(name = "tokenTimeStamp", nullable = true)
	private LocalDateTime _tokenTimeStamp;

	public User() {
	}

	public User(UserDTO dto) {
		this._username = dto.getUsername();
		this._password = dto.getPassword();
		this._firstname = dto.getFirstname();
		this._lastname = dto.getLastname();
	}
	
	public UserDTO convertToDTO() {
		return new UserDTO(_username, _password, _lastname, _firstname);
	}

	public String get_username() {
		return _username;
    }

	public void set_username(String _username) {
		this._username = _username;
    }

    public String get_password() {
        return _password;
    }

    public void set_password(String _password) {
        this._password = _password;
    }

    public String get_firstname() {
        return _firstname;
    }

    public void set_firstname(String _firstname) {
        this._firstname = _firstname;
    }

    public String get_lastname() {
        return _lastname;
    }

    public void set_lastname(String _lastname) {
        this._lastname = _lastname;
    }

	public String get_token() {
		return _token;
	}

	public void set_token(String _token) {
		this._token = _token;
	}

	public LocalDateTime get_tokenTimeStamp() {
		return _tokenTimeStamp;
	}

	public void set_tokenTimeStamp(LocalDateTime _tokenTimeStamp) {
		this._tokenTimeStamp = _tokenTimeStamp;
	}
	
	 public String get_number() {
		return _number;
	}

	public void set_number(String _number) {
		this._number = _number;
	}

	public CreditCardDTO.Type get_type() {
		return _type;
	}

	public void set_type(CreditCardDTO.Type _type) {
		this._type = _type;
	}

	public String get_name() {
		return _name;
	}

	public void set_name(String _name) {
		this._name = _name;
	}

	public LocalDate get_expiryDate() {
		return _expiryDate;
	}

	public void set_expiryDate(LocalDate _expiryDate) {
		this._expiryDate = _expiryDate;
	}
	
	public void set_creditCard(CreditCardDTO dto) {
		this._name = dto.getName();
		this._number = dto.getNumber();
		this._expiryDate = dto.getExpiryDate();
		this._type = dto.getType();
	}
	
	public boolean existingCreditCard() {
		if (this._name != null && this._number != null && this._expiryDate != null && this._type != null) {
			return true;
		}
		return false;
	}
	
}
