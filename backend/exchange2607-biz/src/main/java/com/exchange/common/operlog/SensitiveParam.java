package com.exchange.common.operlog;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记入参中需要脱敏的字段。
 * <p>
 * 加在 Param 字段上，操作日志会把该字段的值替换为 {@code ***}。
 * <p>
 * <b>不标注 = 不脱敏。</b>新增含密码、密钥、验证码的 Param 字段时务必标注，
 * 否则会原样写入 {@code oper_log.request_param}。
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SensitiveParam {

}
