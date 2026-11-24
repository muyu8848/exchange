package com.exchange.member.domain;

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
@Table(name = "country_area_code")
@DynamicInsert(true)
@DynamicUpdate(true)
public class CountryAreaCode implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	@Column(name = "area_code")
	private String areaCode;

	@Column(name = "country_name")
	private String countryName;

	@Column(name = "country_name_en")
	private String countryNameEn;

	@Column(name = "flag_image")
	private String flagImage;

	private String status;

	private Integer sortNum;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

}
