package com.exchange.common.googleauth;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class GoogleAuthInfoVO {

	private String userName;

	private String googleSecretKey;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime googleAuthBindTime;

	public static GoogleAuthInfoVO convertFor(String userName, String googleSecretKey,
			LocalDateTime googleAuthBindTime) {
		GoogleAuthInfoVO vo = new GoogleAuthInfoVO();
		vo.setUserName(userName);
		vo.setGoogleSecretKey(googleSecretKey);
		vo.setGoogleAuthBindTime(googleAuthBindTime);
		return vo;
	}

}
