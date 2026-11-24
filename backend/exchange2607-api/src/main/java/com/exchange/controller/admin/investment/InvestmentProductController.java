package com.exchange.controller.admin.investment;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.investment.param.AddOrUpdateInvestmentProductParam;
import com.exchange.investment.param.InvestmentProductQueryCondParam;
import com.exchange.investment.service.InvestmentProductService;
import com.exchange.investment.vo.InvestmentProductVO;

import cn.dev33.satoken.annotation.SaCheckPermission;

@RestController
@RequestMapping("/investmentProduct")
public class InvestmentProductController {

	@Autowired
	private InvestmentProductService investmentProductService;

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_理财产品, operate = "删除")
	@SaCheckPermission("investmentProduct.del")
	@PostMapping("/del")
	public Result<String> del(String id) {
		investmentProductService.del(id);
		return Result.success();
	}

	@PostMapping("/findById")
	public Result<InvestmentProductVO> findById(String id) {
		return Result.success(investmentProductService.findById(id));
	}

	@PostMapping("/findByPage")
	public Result<PageResult<InvestmentProductVO>> findByPage(InvestmentProductQueryCondParam param) {
		return Result.success(investmentProductService.findByPage(param));
	}

	@PostMapping("/findAll")
	public Result<List<InvestmentProductVO>> findAll(InvestmentProductQueryCondParam param) {
		return Result.success(investmentProductService.findAll(param));
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_理财产品, operate = "新增或修改")
	@SaCheckPermission("investmentProduct.save")
	@PostMapping("/addOrUpdate")
	public Result<String> addOrUpdate(@RequestBody AddOrUpdateInvestmentProductParam param) {
		investmentProductService.addOrUpdate(param);
		return Result.success();
	}

}
