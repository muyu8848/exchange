package com.exchange.log.vo;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.exchange.dictconfig.DictHolder;
import com.exchange.dictconfig.vo.DictItemVO;
import com.exchange.log.domain.MemberAssetChangeLog;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class ExportMemberAssetChangeLogDataVO {

	@ExcelProperty("会员账号")
	private Long memberUid;

	@ExcelProperty("账户类型")
	private String accountType;

	@ExcelProperty("币种")
	private String assetCode;

	@ExcelProperty("业务订单号")
	private String bizOrderNo;

	@ExcelProperty("变动类型")
	private String changeTypeName;

	@ExcelProperty("变动时间")
	@DateTimeFormat("yyyy-MM-dd HH:mm:ss")
	private LocalDateTime changeTime;

	@ExcelProperty("变动前")
	private BigDecimal balanceBefore;

	@ExcelProperty("变化")
	private BigDecimal balanceChange;

	@ExcelProperty("变动后")
	private BigDecimal balanceAfter;

	public static List<ExportMemberAssetChangeLogDataVO> convertFor(List<MemberAssetChangeLog> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		Map<String, String> changeTypeMap = DictHolder.findDictItem("memberAssetChangeType").stream()
				.collect(Collectors.toMap(DictItemVO::getDictItemCode, DictItemVO::getDictItemName));
		List<ExportMemberAssetChangeLogDataVO> vos = new ArrayList<>();
		for (MemberAssetChangeLog po : pos) {
			ExportMemberAssetChangeLogDataVO vo = new ExportMemberAssetChangeLogDataVO();
			BeanUtils.copyProperties(po, vo);
			vo.setChangeTypeName(changeTypeMap.get(po.getChangeType()));
			if (po.getMemberAccount() != null) {
				vo.setMemberUid(po.getMemberAccount().getUid());
			}
			vos.add(vo);
		}
		return vos;
	}

}
