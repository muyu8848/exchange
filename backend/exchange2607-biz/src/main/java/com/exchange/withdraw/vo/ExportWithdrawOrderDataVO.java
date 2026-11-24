package com.exchange.withdraw.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.exchange.dictconfig.DictHolder;
import com.exchange.withdraw.domain.WithdrawOrder;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class ExportWithdrawOrderDataVO {

	@ExcelProperty("订单号")
	private String orderNo;

	@ExcelProperty("会员账号")
	private Long memberUid;

	@ExcelProperty("链类型")
	private String chainType;

	@ExcelProperty("提币地址")
	private String withdrawalAddress;

	@ExcelProperty("币种")
	private String assetCode;

	@ExcelProperty("提币数量")
	private BigDecimal amount;

	@ExcelProperty("手续费")
	private BigDecimal fee;

	@ExcelProperty("实际到账")
	private BigDecimal actualAmount;

	@ExcelProperty("打款USDT")
	private BigDecimal paidUsdtAmount;

	@ExcelProperty("状态")
	private String statusName;

	@ExcelProperty("交易哈希")
	private String txHash;

	@ExcelProperty("审核备注")
	private String auditRemark;

	@ExcelProperty("提交时间")
	@DateTimeFormat("yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	@ExcelProperty("完成时间")
	@DateTimeFormat("yyyy-MM-dd HH:mm:ss")
	private LocalDateTime completeTime;

	public static List<ExportWithdrawOrderDataVO> convertFor(List<WithdrawOrder> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<ExportWithdrawOrderDataVO> vos = new ArrayList<>();
		for (WithdrawOrder po : pos) {
			ExportWithdrawOrderDataVO vo = new ExportWithdrawOrderDataVO();
			BeanUtils.copyProperties(po, vo);
			vo.setStatusName(DictHolder.getDictItemName("withdrawOrderStatus", po.getStatus()));
			if (po.getMemberAccount() != null) {
				vo.setMemberUid(po.getMemberAccount().getUid());
			}
			vos.add(vo);
		}
		return vos;
	}

}
