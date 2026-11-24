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
import com.exchange.investment.param.InvestmentInterestLogQueryCondParam;
import com.exchange.investment.service.InvestmentInterestLogQueryService;
import com.exchange.investment.vo.ExportInvestmentInterestLogDataVO;
import com.exchange.investment.vo.InvestmentInterestLogSubtotalVO;
import com.exchange.investment.vo.InvestmentInterestLogVO;

@RestController
@RequestMapping("/investmentInterestLog")
public class InvestmentInterestLogController {

	@Autowired
	private InvestmentInterestLogQueryService investmentInterestLogQueryService;

	@PostMapping("/findByPage")
	public Result<PageResult<InvestmentInterestLogVO>> findByPage(InvestmentInterestLogQueryCondParam param) {
		return Result.success(investmentInterestLogQueryService.findInvestmentInterestLogByPage(param));
	}

	@PostMapping("/subtotal")
	public Result<InvestmentInterestLogSubtotalVO> subtotal(InvestmentInterestLogQueryCondParam param) {
		return Result.success(investmentInterestLogQueryService.interestLogSubtotal(param));
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_理财收益流水, operate = "导出")
	@PostMapping("/exportData")
	public void exportData(HttpServletResponse response, InvestmentInterestLogQueryCondParam param) throws IOException {
		List<ExportInvestmentInterestLogDataVO> datas = investmentInterestLogQueryService.findInterestLogExportData(param);
		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("utf-8");
		String fileName = URLEncoder.encode("理财收益流水导出", "UTF-8");
		response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
		EasyExcel.write(response.getOutputStream(), ExportInvestmentInterestLogDataVO.class).sheet("导出数据").doWrite(datas);
	}

}
