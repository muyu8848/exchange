package com.exchange.controller.admin.log;

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
import com.exchange.log.param.MemberAssetChangeLogQueryCondParam;
import com.exchange.log.service.MemberAssetChangeLogService;
import com.exchange.log.vo.ExportMemberAssetChangeLogDataVO;
import com.exchange.log.vo.MemberAssetChangeLogVO;
import com.exchange.log.vo.MemberAssetChangeSubtotalVO;

/**
 * 会员资金流水
 */
@RestController
@RequestMapping("/memberAssetChangeLog")
public class MemberAssetChangeLogController {

	@Autowired
	private MemberAssetChangeLogService memberAssetChangeLogService;
	
	/**
	 * 导出数据
	 * @param response
	 * @param param
	 * @throws IOException
	 */
	@PostMapping("/exportData")
	public void exportData(HttpServletResponse response, MemberAssetChangeLogQueryCondParam param)
			throws IOException {
		List<ExportMemberAssetChangeLogDataVO> datas = memberAssetChangeLogService
				.findExportBalanceChangeData(param);
		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("utf-8");
		String fileName = URLEncoder.encode("导出数据", "UTF-8");
		response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
		EasyExcel.write(response.getOutputStream(), ExportMemberAssetChangeLogDataVO.class).sheet("导出数据")
				.doWrite(datas);
	}

	/**
	 * 小计
	 * @param param
	 * @return
	 */
	@PostMapping("/subtotal")
	public Result<MemberAssetChangeSubtotalVO> subtotal(MemberAssetChangeLogQueryCondParam param) {
		return Result.success(memberAssetChangeLogService.subtotal(param));
	}
	
	/**
	 * 分页获取记录
	 * @param param
	 * @return
	 */
	@PostMapping("/findByPage")
	public Result<PageResult<MemberAssetChangeLogVO>> findByPage(MemberAssetChangeLogQueryCondParam param) {
		return Result.success(memberAssetChangeLogService.findByPage(param));
	}

}
