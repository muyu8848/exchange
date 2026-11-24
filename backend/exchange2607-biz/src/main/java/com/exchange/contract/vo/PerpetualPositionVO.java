package com.exchange.contract.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.SymbolPriceUtils;
import com.exchange.constants.Constant;
import com.exchange.contract.domain.PerpetualContractSymbol;
import com.exchange.contract.domain.PerpetualPosition;
import com.exchange.dictconfig.DictHolder;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;

@Data
public class PerpetualPositionVO {

	private String id;
	
	private String orderNo;
	
	private String memberAccountId;
	
	private Long memberUid;
	
	private String symbolCode;
	
	private String direction;
	
	private String directionName;
	
	private Integer leverage;
	
	private BigDecimal margin;
	
	private BigDecimal size;
	
	private BigDecimal openPrice;
	
	private BigDecimal liquidationPrice;
	
	private BigDecimal accumulatedFunding;
	
	private String status;
	
	private String statusName;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime openTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime closeTime;

	private BigDecimal closePrice;
	private BigDecimal realizedPnl;

	/** 实时未实现盈亏(查询时计算) */
	private BigDecimal unrealizedPnl;

	/** 收益率(%)，= 未实现盈亏 ÷ 保证金 × 100(查询时计算) */
	private BigDecimal pnlRate;

	/** 实时保证金率(查询时计算) */
	private BigDecimal marginRatio;

	/** 当前标记价 */
	private BigDecimal markPrice;

	public static List<PerpetualPositionVO> convertFor(List<PerpetualPosition> pos, int pnlScale) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<PerpetualPositionVO> vos = new ArrayList<>();
		for (PerpetualPosition po : pos) {
			vos.add(convertFor(po, pnlScale));
		}
		return vos;
	}

	public static PerpetualPositionVO convertFor(PerpetualPosition po, int pnlScale) {
		if (po == null) {
			return null;
		}
		PerpetualPositionVO vo = new PerpetualPositionVO();
		BeanUtils.copyProperties(po, vo);
		vo.setDirectionName(DictHolder.getDictItemName("perpetualDirection", po.getDirection()));
		vo.setStatusName(DictHolder.getDictItemName("perpetualPositionStatus", po.getStatus()));
		if (po.getMemberAccount() != null) {
			vo.setMemberUid(po.getMemberAccount().getUid());
		}

		// 计算实时盈亏（纯计算复用 PerpetualPosition 实体方法）
		if (Constant.永续持仓状态_持仓中.equals(po.getStatus())) {
			String priceStr = SymbolPriceUtils.getClosePrice(Constant.行情业务类型_合约, po.getSymbolCode());
			if (StrUtil.isNotBlank(priceStr)) {
				BigDecimal markPrice = new BigDecimal(priceStr);
				// 行情价格按交易对价格精度截断（OKX 推送精度不固定）；交易对配置缺失时保持原值
				PerpetualContractSymbol symbol = po.getSymbol();
				Integer priceScale = symbol == null ? null : symbol.getPriceScale();
				if (priceScale != null) {
					markPrice = markPrice.setScale(priceScale, RoundingMode.DOWN);
				}
				vo.setMarkPrice(markPrice);
				BigDecimal pnl = po.calcUnrealizedPnl(markPrice, pnlScale);
				vo.setUnrealizedPnl(pnl);
				vo.setPnlRate(po.calcPnlRate(pnl, 4));
				vo.setMarginRatio(po.calcMarginRatio(markPrice, pnl, 4));
			}
		}
		return vo;
	}

}
