package com.exchange.common.json;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * BigDecimal序列化器：去除末尾多余零 + toPlainString杜绝科学计数
 */
public class BigDecimalStripTrailingZerosToPlainString extends JsonSerializer<BigDecimal> {

	@Override
	public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		if (value == null) {
			gen.writeNull();
			return;
		}
		BigDecimal stripped = value.stripTrailingZeros();
		// Java 8 bug: stripTrailingZeros() 对 0.00000000 仍返回 0E-8 而非 0（JDK-8211400，Java
		// 11修复）
		if (stripped.compareTo(BigDecimal.ZERO) == 0) {
			gen.writeString("0");
			return;
		}
		gen.writeString(stripped.toPlainString());
	}
}
