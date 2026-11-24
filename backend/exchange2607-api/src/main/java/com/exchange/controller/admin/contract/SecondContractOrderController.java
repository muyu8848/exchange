package com.exchange.controller.admin.contract;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.excel.EasyExcel;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.contract.param.SecondContractOrderQueryCondParam;
import com.exchange.contract.service.SecondContractOrderService;
import com.exchange.contract.vo.ExportSecondContractOrderDataVO;
import com.exchange.contract.vo.SecondContractOrderSubtotalVO;
import com.exchange.contract.vo.SecondContractOrderVO;

@Validated
@RestController
@RequestMapping("/secondContractOrder")
public class SecondContractOrderController {

	@Autowired
	private SecondContractOrderService secondContractOrderService;

	@PostMapping("findSecondContractOrderByPage")
	public Result<PageResult<SecondContractOrderVO>> findSecondContractOrderByPage(
			SecondContractOrderQueryCondParam param) {
		return Result.success(secondContractOrderService.findSecondContractOrderByPage(param));
	}

	@PostMapping("findById")
	public Result<SecondContractOrderVO> findById(String id) {
		return Result.success(secondContractOrderService.findById(id));
	}

	@PostMapping("subtotal")
	public Result<SecondContractOrderSubtotalVO> subtotal(SecondContractOrderQueryCondParam param) {
		return Result.success(secondContractOrderService.subtotal(param));
	}

	@PostMapping("export")
	public void export(HttpServletResponse response, SecondContractOrderQueryCondParam param) throws IOException {
		List<ExportSecondContractOrderDataVO> datas = secondContractOrderService.findExportData(param);
		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("utf-8");
		String fileName = URLEncoder.encode("秒合约订单", "UTF-8");
		response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
		EasyExcel.write(response.getOutputStream(), ExportSecondContractOrderDataVO.class).sheet("秒合约订单").doWrite(datas);
	}

}
