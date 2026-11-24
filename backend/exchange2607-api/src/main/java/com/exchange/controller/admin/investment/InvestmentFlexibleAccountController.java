package com.exchange.controller.admin.investment;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.excel.EasyExcel;
import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.investment.param.InvestmentFlexibleAccountQueryCondParam;
import com.exchange.investment.service.InvestmentPositionService;
import com.exchange.investment.vo.ExportInvestmentFlexibleAccountDataVO;
import com.exchange.investment.vo.InvestmentFlexibleAccountSubtotalVO;
import com.exchange.investment.vo.InvestmentFlexibleAccountVO;

@RestController
@RequestMapping("/investmentFlexibleAccount")
public class InvestmentFlexibleAccountController {

	@Autowired
	private InvestmentPositionService investmentPositionService;

	@PostMapping("/findDetail")
	public Result<InvestmentFlexibleAccountVO> findDetail(String id) {
		return Result.success(investmentPositionService.findFlexibleAccountDetail(id));
	}

	@PostMapping("/findByPage")
	public Result<PageResult<InvestmentFlexibleAccountVO>> findByPage(InvestmentFlexibleAccountQueryCondParam param) {
		return Result.success(investmentPositionService.findFlexibleAccountPage(param));
	}

	@PostMapping("/subtotal")
	public Result<InvestmentFlexibleAccountSubtotalVO> subtotal(InvestmentFlexibleAccountQueryCondParam param) {
		return Result.success(investmentPositionService.flexibleAccountSubtotal(param));
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_活期赚币账户, operate = "导出")
	@PostMapping("/exportData")
	public void exportData(HttpServletResponse response, InvestmentFlexibleAccountQueryCondParam param)
			throws IOException {
		List<ExportInvestmentFlexibleAccountDataVO> datas = investmentPositionService
				.findFlexibleAccountExportData(param);
		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("utf-8");
		String fileName = URLEncoder.encode("活期赚币账户导出", "UTF-8");
		response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
		EasyExcel.write(response.getOutputStream(), ExportInvestmentFlexibleAccountDataVO.class).sheet("导出数据")
				.doWrite(datas);
	}

}
