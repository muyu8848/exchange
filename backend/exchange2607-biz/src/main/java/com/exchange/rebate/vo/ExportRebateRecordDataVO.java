package com.exchange.rebate.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.exchange.dictconfig.DictHolder;
import com.exchange.rebate.domain.RebateRecord;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 返佣记录导出 VO（EasyExcel）
 */
@Data
public class ExportRebateRecordDataVO {

	@ExcelProperty("受益人账号")
	private Long beneficiaryUid;

	@ExcelProperty("来源会员账号")
	private Long sourceMemberUid;

	@ExcelProperty("层级")
	private Integer level;

	@ExcelProperty("返佣金额")
	private BigDecimal amount;

	@ExcelProperty("返佣币种")
	private String assetCode;

	@ExcelProperty("业务类型")
	private String bizTypeName;

	@ExcelProperty("来源订单号")
	private String sourceOrderId;

	@ExcelProperty("返佣时间")
	@DateTimeFormat("yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	public static List<ExportRebateRecordDataVO> convertFor(List<RebateRecord> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<ExportRebateRecordDataVO> vos = new ArrayList<>();
		for (RebateRecord po : pos) {
			ExportRebateRecordDataVO vo = new ExportRebateRecordDataVO();
			vo.setLevel(po.getLevel());
			vo.setAmount(po.getAmount());
			vo.setAssetCode(po.getAssetCode());
			vo.setSourceOrderId(po.getSourceOrderId());
			vo.setCreateTime(po.getCreateTime());
			vo.setBizTypeName(DictHolder.getDictItemName("rebateBizType", po.getBizType()));
			vo.setBeneficiaryUid(po.getBeneficiary().getUid());
			vo.setSourceMemberUid(po.getSourceMember().getUid());
			vos.add(vo);
		}
		return vos;
	}
}
