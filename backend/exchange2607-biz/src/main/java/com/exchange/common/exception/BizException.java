package com.exchange.common.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer code;

	/** 国际化文案 key（= 中文原文 / 中文模板） */
	private String msg;

	/** 文案占位符参数，配合中文模板使用，如「数量精度不能超过{0}位」 */
	private Object[] args;

	public BizException(BizError bizError) {
		super(bizError.getMsg());
		this.code = bizError.getCode();
		this.msg = bizError.getMsg();
	}
	
	public BizException(String msg) {
		super(msg);
		this.code = BizError.业务异常.getCode();
		this.msg = msg;
	}

	public BizException(String msg, Object... args) {
		super(msg);
		this.code = BizError.业务异常.getCode();
		this.msg = msg;
		this.args = args;
	}

	public BizException(Integer code, String msg) {
		super(msg);
		this.code = code;
		this.msg = msg;
	}

}
