package com.exchange.todo.vo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class TodoListVO {

	private long totalCount;

	private List<TodoItemVO> items = new ArrayList<>();

}
