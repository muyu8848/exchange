package com.exchange.background.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "role")
@DynamicInsert(true)
@DynamicUpdate(true)
public class Role implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	private String name;

	private LocalDateTime createTime;

	private Boolean deletedFlag;

	private LocalDateTime deletedTime;

	public void deleted() {
		this.setDeletedFlag(true);
		this.setDeletedTime(LocalDateTime.now());
	}

}
