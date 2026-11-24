package com.exchange.controller.admin.spot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.spot.param.ConvertOrderQueryCondParam;
import com.exchange.spot.service.ConvertService;
import com.exchange.spot.vo.ConvertOrderSubtotalVO;
import com.exchange.spot.vo.ConvertOrderVO;

@Validated
@RestController
@RequestMapping("/convertOrder")
public class ConvertOrderController {

	@Autowired
	private ConvertService convertService;

	@PostMapping("/findOrderByPage")
	public Result<PageResult<ConvertOrderVO>> findOrderByPage(ConvertOrderQueryCondParam param) {
		return Result.success(convertService.findByPage(param));
	}

	@PostMapping("/findOrderById")
	public Result<ConvertOrderVO> findOrderById(String id) {
		return Result.success(convertService.findById(id));
	}

	@PostMapping("/subtotal")
	public Result<ConvertOrderSubtotalVO> subtotal(ConvertOrderQueryCondParam param) {
		return Result.success(convertService.subtotal(param));
	}
}
