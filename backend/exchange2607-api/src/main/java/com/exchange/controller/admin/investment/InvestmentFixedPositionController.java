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
import com.exchange.investment.param.InvestmentFixedPositionQueryCondParam;
import com.exchange.investment.service.InvestmentInterestLogQueryService;
import com.exchange.investment.service.InvestmentPositionService;
import com.exchange.investment.vo.ExportInvestmentFixedPositionDataVO;
import com.exchange.investment.vo.InvestmentFixedPositionVO;
import com.exchange.investment.vo.InvestmentInterestLogVO;
import com.exchange.investment.vo.InvestmentPositionSubtotalVO;

@RestController
@RequestMapping("/investmentFixedPosition")
public class InvestmentFixedPositionController {

	@Autowired
	private InvestmentPositionService investmentPositionService;

	@Autowired
	private InvestmentInterestLogQueryService investmentInterestLogQueryService;

	@PostMapping("/findDetail")
	public Result<InvestmentFixedPositionVO> findDetail(String id) {
		return Result.success(investmentPositionService.findFixedPositionDetail(id));
	}

	@PostMapping("/findByPage")
	public Result<PageResult<InvestmentFixedPositionVO>> findByPage(InvestmentFixedPositionQueryCondParam param) {
		return Result.success(investmentPositionService.findFixedPositionPage(param));
	}

	@PostMapping("/subtotal")
	public Result<InvestmentPositionSubtotalVO> subtotal(InvestmentFixedPositionQueryCondParam param) {
		return Result.success(investmentPositionService.fixedPositionSubtotal(param));
	}

	@PostMapping("/findInterestLogByPage")
	public Result<List<InvestmentInterestLogVO>> findInterestLogByPage(String positionId) {
		return Result.success(investmentInterestLogQueryService.findPositionInterestLogs(positionId));
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_定期持仓, operate = "导出")
	@PostMapping("/exportData")
	public void exportData(HttpServletResponse response, InvestmentFixedPositionQueryCondParam param) throws IOException {
		List<ExportInvestmentFixedPositionDataVO> datas = investmentPositionService.findFixedPositionExportData(param);
		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("utf-8");
		String fileName = URLEncoder.encode("定期持仓导出", "UTF-8");
		response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
		EasyExcel.write(response.getOutputStream(), ExportInvestmentFixedPositionDataVO.class).sheet("导出数据").doWrite(datas);
	}

}
