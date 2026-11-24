package com.exchange.investment.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.exchange.dictconfig.DictHolder;
import com.exchange.investment.domain.InvestmentInterestLog;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class ExportInvestmentInterestLogDataVO {

	@ExcelProperty("会员账号")
	private Long memberUid;

	@ExcelProperty("币种")
	private String assetCode;

	@ExcelProperty("产品类型")
	private String productTypeName;

	@ExcelProperty("收益类型")
	private String interestTypeName;

	@ExcelProperty("持仓订单号")
	private String positionId;

	@ExcelProperty("计息日")
	@DateTimeFormat("yyyy-MM-dd")
	private LocalDate interestDate;

	@ExcelProperty("本金")
	private BigDecimal principal;

	@ExcelProperty("收益金额")
	private BigDecimal interestAmount;

	@ExcelProperty("入账时间")
	@DateTimeFormat("yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	public static List<ExportInvestmentInterestLogDataVO> convertFor(List<InvestmentInterestLog> logs) {
		if (CollectionUtil.isEmpty(logs)) {
			return new ArrayList<>();
		}
		List<ExportInvestmentInterestLogDataVO> vos = new ArrayList<>();
		for (InvestmentInterestLog log : logs) {
			ExportInvestmentInterestLogDataVO vo = new ExportInvestmentInterestLogDataVO();
			BeanUtils.copyProperties(log, vo);
			vo.setProductTypeName(DictHolder.getDictItemName("investmentProductType", log.getProductType()));
			vo.setInterestTypeName(DictHolder.getDictItemName("investmentInterestType", log.getInterestType()));
			if (log.getMemberAccount() != null) {
				vo.setMemberUid(log.getMemberAccount().getUid());
			}
			vos.add(vo);
		}
		return vos;
	}

}
