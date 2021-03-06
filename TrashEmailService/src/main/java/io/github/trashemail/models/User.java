package io.github.trashemail.models;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name="UserEmailToChatIdMapping")
@Getter
@Setter
@NoArgsConstructor
public class User {
	@Id
	@GeneratedValue
	private Integer id;
	private long chatId;

	@Column(unique = true)
	private String emailId;

	private String forwardsTo;

	@CreationTimestamp
	private LocalDateTime createDateTime;

	public User(long chatId, String emailId, String forwardsTo) {
		this.chatId = chatId;
		this.emailId = emailId;
		this.forwardsTo = forwardsTo;
	}

	@Override
	public java.lang.String toString() {
		return this.emailId;
	}
}
