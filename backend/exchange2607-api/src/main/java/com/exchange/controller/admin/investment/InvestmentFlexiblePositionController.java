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
import com.exchange.investment.param.InvestmentFlexiblePositionQueryCondParam;
import com.exchange.investment.service.InvestmentInterestLogQueryService;
import com.exchange.investment.service.InvestmentPositionService;
import com.exchange.investment.vo.ExportInvestmentFlexiblePositionDataVO;
import com.exchange.investment.vo.InvestmentFlexiblePositionSubtotalVO;
import com.exchange.investment.vo.InvestmentFlexiblePositionVO;
import com.exchange.investment.vo.InvestmentInterestLogVO;

@RestController
@RequestMapping("/investmentFlexiblePosition")
public class InvestmentFlexiblePositionController {

	@Autowired
	private InvestmentPositionService investmentPositionService;

	@Autowired
	private InvestmentInterestLogQueryService investmentInterestLogQueryService;

	@PostMapping("/findDetail")
	public Result<InvestmentFlexiblePositionVO> findDetail(String id) {
		return Result.success(investmentPositionService.findFlexiblePositionDetail(id));
	}

	@PostMapping("/findByPage")
	public Result<PageResult<InvestmentFlexiblePositionVO>> findByPage(InvestmentFlexiblePositionQueryCondParam param) {
		return Result.success(investmentPositionService.findFlexiblePositionPage(param));
	}

	@PostMapping("/subtotal")
	public Result<InvestmentFlexiblePositionSubtotalVO> subtotal(InvestmentFlexiblePositionQueryCondParam param) {
		return Result.success(investmentPositionService.flexiblePositionSubtotal(param));
	}

	@PostMapping("/findInterestLogByPage")
	public Result<List<InvestmentInterestLogVO>> findInterestLogByPage(String positionId) {
		return Result.success(investmentInterestLogQueryService.findPositionInterestLogs(positionId));
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_活期持仓, operate = "导出")
	@PostMapping("/exportData")
	public void exportData(HttpServletResponse response, InvestmentFlexiblePositionQueryCondParam param) throws IOException {
		List<ExportInvestmentFlexiblePositionDataVO> datas = investmentPositionService.findFlexiblePositionExportData(param);
		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("utf-8");
		String fileName = URLEncoder.encode("活期持仓导出", "UTF-8");
		response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
		EasyExcel.write(response.getOutputStream(), ExportInvestmentFlexiblePositionDataVO.class).sheet("导出数据").doWrite(datas);
	}

}
