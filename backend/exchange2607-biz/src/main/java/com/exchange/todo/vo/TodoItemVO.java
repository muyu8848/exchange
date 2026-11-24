package com.exchange.todo.vo;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

@Data
public class TodoItemVO {

	private String key;

	private String label;

	private long count;

	private String menuUrl;

	private String group;

	private String groupName;

	private Map<String, String> query = new LinkedHashMap<>();

}
