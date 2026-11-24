package com.exchange.member.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.exchange.dictconfig.DictHolder;
import com.exchange.member.domain.AssetTransferOrder;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class ExportAssetTransferOrderVO {

	@ExcelProperty("订单号")
	@ColumnWidth(28)
	private String orderNo;

	@ExcelProperty("会员账号")
	@ColumnWidth(16)
	private Long memberUid;

	@ExcelProperty("划出账户")
	@ColumnWidth(12)
	private String fromAccountTypeName;

	@ExcelProperty("划入账户")
	@ColumnWidth(12)
	private String toAccountTypeName;

	@ExcelProperty("币种")
	@ColumnWidth(10)
	private String assetCode;

	@ExcelProperty("划转金额")
	@ColumnWidth(16)
	private BigDecimal amount;

	@ExcelProperty("时间")
	@ColumnWidth(20)
	private String createTime;

	public static List<ExportAssetTransferOrderVO> convertFor(List<AssetTransferOrder> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<ExportAssetTransferOrderVO> vos = new ArrayList<>();
		for (AssetTransferOrder po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static ExportAssetTransferOrderVO convertFor(AssetTransferOrder po) {
		if (po == null) {
			return null;
		}
		ExportAssetTransferOrderVO vo = new ExportAssetTransferOrderVO();
		vo.setOrderNo(po.getOrderNo());
		vo.setMemberUid(po.getMemberAccount() != null ? po.getMemberAccount().getUid() : null);
		vo.setFromAccountTypeName(DictHolder.getDictItemName("assetAccountType", po.getFromAccountType()));
		vo.setToAccountTypeName(DictHolder.getDictItemName("assetAccountType", po.getToAccountType()));
		vo.setAssetCode(po.getAssetCode());
		vo.setAmount(po.getAmount());
		vo.setCreateTime(po.getCreateTime() != null ? po.getCreateTime().toString() : null);
		return vo;
	}

}
