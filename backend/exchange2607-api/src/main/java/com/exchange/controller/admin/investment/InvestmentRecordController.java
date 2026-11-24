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
import com.exchange.investment.param.InvestmentRecordQueryCondParam;
import com.exchange.investment.service.InvestmentPositionService;
import com.exchange.investment.vo.ExportInvestmentRecordDataVO;
import com.exchange.investment.vo.InvestmentRecordSubtotalVO;
import com.exchange.investment.vo.InvestmentRecordVO;

@RestController
@RequestMapping("/investmentRecord")
public class InvestmentRecordController {

	@Autowired
	private InvestmentPositionService investmentPositionService;

	@PostMapping("/findByPage")
	public Result<PageResult<InvestmentRecordVO>> findByPage(InvestmentRecordQueryCondParam param) {
		return Result.success(investmentPositionService.findInvestmentRecordPage(param));
	}

	@PostMapping("/findDetail")
	public Result<InvestmentRecordVO> findDetail(String id) {
		return Result.success(investmentPositionService.findInvestmentRecordDetail(id));
	}

	@PostMapping("/subtotal")
	public Result<InvestmentRecordSubtotalVO> subtotal(InvestmentRecordQueryCondParam param) {
		return Result.success(investmentPositionService.investmentRecordSubtotal(param));
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_投资记录, operate = "导出")
	@PostMapping("/exportData")
	public void exportData(HttpServletResponse response, InvestmentRecordQueryCondParam param) throws IOException {
		List<ExportInvestmentRecordDataVO> datas = investmentPositionService.findInvestmentRecordExportData(param);
		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("utf-8");
		String fileName = URLEncoder.encode("投资记录导出", "UTF-8");
		response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
		EasyExcel.write(response.getOutputStream(), ExportInvestmentRecordDataVO.class).sheet("导出数据").doWrite(datas);
	}

}
