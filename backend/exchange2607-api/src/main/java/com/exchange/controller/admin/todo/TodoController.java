package com.exchange.controller.admin.todo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.Result;
import com.exchange.todo.service.TodoService;
import com.exchange.todo.vo.TodoListVO;

/**
 * 后台待办清单：供顶栏待办条与登录后轮询使用。
 */
@RestController
@RequestMapping("/todo")
public class TodoController {

	@Autowired
	private TodoService todoService;

	@PostMapping("/findTodoList")
	public Result<TodoListVO> findTodoList() {
		return Result.success(todoService.findTodoList());
	}

}
