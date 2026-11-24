package com.exchange.config.xss;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

	public XssHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	@Override
	public String getHeader(String name) {
		String value = super.getHeader(name);
		if (value != null) {
			value = HtmlUtil.cleanHtmlTag(value);
		}
		return value;
	}

	@Override
	public String getQueryString() {
		String value = super.getQueryString();
		if (value != null) {
			value = HtmlUtil.cleanHtmlTag(value);
		}
		return value;
	}

	@Override
	public String getParameter(String name) {
		String value = super.getParameter(name);
		if (value != null) {
			value = HtmlUtil.cleanHtmlTag(value);
		}
		return value;
	}

	@Override
	public String[] getParameterValues(String name) {
		String[] values = super.getParameterValues(name);
		if (values != null) {
			int length = values.length;
			String[] escapseValues = new String[length];
			for (int i = 0; i < length; i++) {
				escapseValues[i] = HtmlUtil.cleanHtmlTag(values[i]);
			}
			return escapseValues;
		}
		return values;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		// 非json类型，直接返回
		if (!isJsonRequest()) {
			return super.getInputStream();
		}

		// 为空，直接返回
		String json = StrUtil.str(IoUtil.readBytes(super.getInputStream(), false), StandardCharsets.UTF_8);
		if (StrUtil.isEmpty(json)) {
			return super.getInputStream();
		}

		// xss过滤
		json = HtmlUtil.cleanHtmlTag(json);
		byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
		final ByteArrayInputStream bis = IoUtil.toStream(jsonBytes);
		return new ServletInputStream() {
			@Override
			public boolean isFinished() {
				return true;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public int available() throws IOException {
				return jsonBytes.length;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
			}

			@Override
			public int read() throws IOException {
				return bis.read();
			}
		};
	}

	public boolean isJsonRequest() {
		String header = super.getHeader(HttpHeaders.CONTENT_TYPE);
		return StrUtil.startWithIgnoreCase(header, MediaType.APPLICATION_JSON_VALUE);
	}

}
