package com.exchange.controller.admin.member;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.excel.EasyExcel;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.member.param.AssetTransferQueryCondParam;
import com.exchange.member.service.AssetTransferService;
import com.exchange.member.vo.AssetTransferOrderVO;
import com.exchange.member.vo.ExportAssetTransferOrderVO;

@RestController
@RequestMapping("/assetTransfer")
public class AssetTransferController {

	@Autowired
	private AssetTransferService assetTransferService;

	@PostMapping("/findByPage")
	public Result<PageResult<AssetTransferOrderVO>> findByPage(AssetTransferQueryCondParam param) {
		return Result.success(assetTransferService.findByPage(param));
	}

	@PostMapping("/exportData")
	public void exportData(HttpServletResponse response, AssetTransferQueryCondParam param) throws IOException {
		List<ExportAssetTransferOrderVO> datas = assetTransferService.findExportData(param);
		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("utf-8");
		String fileName = URLEncoder.encode("资金划转记录", "UTF-8");
		response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
		EasyExcel.write(response.getOutputStream(), ExportAssetTransferOrderVO.class).sheet("资金划转记录").doWrite(datas);
	}

}
