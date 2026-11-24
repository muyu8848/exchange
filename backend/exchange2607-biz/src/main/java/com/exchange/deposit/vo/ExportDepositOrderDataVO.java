package com.exchange.deposit.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.exchange.deposit.domain.DepositOrder;
import com.exchange.dictconfig.DictHolder;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class ExportDepositOrderDataVO {

	@ExcelProperty("订单号")
	private String orderNo;

	@ExcelProperty("会员账号")
	private Long memberUid;

	@ExcelProperty("链类型")
	private String chainType;

	@ExcelProperty("充币地址")
	private String depositAddress;

	@ExcelProperty("币种")
	private String assetCode;

	@ExcelProperty("充值数量")
	private BigDecimal amount;

	@ExcelProperty("实际到账")
	private BigDecimal actualAmount;

	@ExcelProperty("入账USDT")
	private BigDecimal creditedUsdtAmount;

	@ExcelProperty("入账状态")
	private String entryAccountStatus;

	@ExcelProperty("TxHash")
	private String txHash;

	@ExcelProperty("状态")
	private String statusName;

	@ExcelProperty("创建时间")
	@DateTimeFormat("yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	@ExcelProperty("过期时间")
	@DateTimeFormat("yyyy-MM-dd HH:mm:ss")
	private LocalDateTime expireTime;

	@ExcelProperty("入账时间")
	@DateTimeFormat("yyyy-MM-dd HH:mm:ss")
	private LocalDateTime creditTime;

	@ExcelProperty("关闭原因")
	private String closeReason;

	public static List<ExportDepositOrderDataVO> convertFor(List<DepositOrder> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<ExportDepositOrderDataVO> vos = new ArrayList<>();
		for (DepositOrder po : pos) {
			ExportDepositOrderDataVO vo = new ExportDepositOrderDataVO();
			BeanUtils.copyProperties(po, vo);
			vo.setStatusName(DictHolder.getDictItemName("usdtDepositOrderStatus", po.getStatus()));
			if (po.getMemberAccount() != null) {
				vo.setMemberUid(po.getMemberAccount().getUid());
			}
			vos.add(vo);
		}
		return vos;
	}

}
