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
import com.exchange.investment.domain.InvestmentFixedPosition;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class ExportInvestmentFixedPositionDataVO {

	@ExcelProperty("订单号")
	private String orderNo;

	@ExcelProperty("会员账号")
	private Long memberUid;


	@ExcelProperty("本金")
	private BigDecimal principal;

	@ExcelProperty("封闭天数")
	private Integer lockDays;

	@ExcelProperty("估算总收益")
	private BigDecimal estimatedTotalInterest;

	@ExcelProperty("已确认收益")
	private BigDecimal confirmedInterest;

	@ExcelProperty("状态")
	private String statusName;

	@ExcelProperty("申购时间")
	@DateTimeFormat("yyyy-MM-dd HH:mm:ss")
	private LocalDateTime investTime;

	@ExcelProperty("起息日")
	@DateTimeFormat("yyyy-MM-dd")
	private LocalDate valueDate;

	@ExcelProperty("到期日")
	@DateTimeFormat("yyyy-MM-dd")
	private LocalDate maturityDate;

	@ExcelProperty("到账日")
	@DateTimeFormat("yyyy-MM-dd")
	private LocalDate arrivalDate;

	@ExcelProperty("下次计息时间")
	@DateTimeFormat("yyyy-MM-dd HH:mm:ss")
	private LocalDateTime nextAccrualTime;

	@ExcelProperty("赎回时间")
	@DateTimeFormat("yyyy-MM-dd HH:mm:ss")
	private LocalDateTime redeemTime;

	public static List<ExportInvestmentFixedPositionDataVO> convertFor(List<InvestmentFixedPosition> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<ExportInvestmentFixedPositionDataVO> vos = new ArrayList<>();
		for (InvestmentFixedPosition po : pos) {
			ExportInvestmentFixedPositionDataVO vo = new ExportInvestmentFixedPositionDataVO();
			BeanUtils.copyProperties(po, vo);
			if (po.getMemberAccount() != null) {
				vo.setMemberUid(po.getMemberAccount().getUid());
			}
			vo.setStatusName(DictHolder.getDictItemName("investmentPositionStatus", po.getStatus()));
			vo.setEstimatedTotalInterest(po.getEstimatedTotalInterest());
			vo.setConfirmedInterest(po.getConfirmedInterest());
			vos.add(vo);
		}
		return vos;
	}

}
