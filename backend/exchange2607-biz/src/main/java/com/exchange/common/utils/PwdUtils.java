package com.exchange.common.utils;

import cn.dev33.satoken.secure.SaSecureUtil;

public class PwdUtils {

	public static Boolean pwdVerify(String plainPwd, String encryptPwd) {
		return SaSecureUtil.sha256(plainPwd).equals(encryptPwd);
	}
	
	public static String pwdEncrypt(String plainPwd) {
		return SaSecureUtil.sha256(plainPwd);
	}

}
