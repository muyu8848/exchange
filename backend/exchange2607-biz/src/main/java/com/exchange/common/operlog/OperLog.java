package com.exchange.common.operlog;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {
	
	public String subSystem() default "";

	public String module() default "";

	public String operate() default "";

	/**
	 * 权限节点名称：反射生成 menu_3 时使用；为空则取 {@link #operate()}。
	 * <p>
	 * 仅当同一个权限码被多个接口复用时才需要显式指定——此时各接口的 operate 各不相同
	 * （「上移」「下移」「新增或修改」），但权限名应统一概括（「菜单维护（含排序）」）。
	 * <p>
	 * 注意：这个字段只影响生成的权限名，不影响操作日志展示。
	 */
	public String permissionName() default "";

}
