package com.exchange.common.vo;

import lombok.Data;

@Data
public class AsynNoticeResult {

	private String notifyUrl;

	private String contentType = "application/x-www-form-urlencoded";

	private String requestContent;

	private String responseContent;

	private Boolean successFlag;

	public static AsynNoticeResult build(Boolean successFlag, String notifyUrl, String requestContent,
			String responseContent) {
		AsynNoticeResult vo = new AsynNoticeResult();
		vo.setSuccessFlag(successFlag);
		vo.setNotifyUrl(notifyUrl);
		vo.setRequestContent(requestContent);
		vo.setResponseContent(responseContent);
		return vo;
	}

	public static AsynNoticeResult fail() {
		AsynNoticeResult vo = new AsynNoticeResult();
		vo.setSuccessFlag(false);
		return vo;
	}

}
