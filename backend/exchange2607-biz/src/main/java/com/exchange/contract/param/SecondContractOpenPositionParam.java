package com.exchange.contract.param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.IdUtils;
import com.exchange.constants.Constant;
import com.exchange.contract.domain.SecondContractOrder;

import lombok.Data;

@Data
public class SecondContractOpenPositionParam {

	private String memberAccountId;

	@NotBlank
	private String symbolCode;

	@NotNull
	private Integer period;

	@NotBlank
	private String direction;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	@Digits(integer = Constant.整数部分位数, fraction = Constant.小数部分位数)
	private BigDecimal amount;

	public SecondContractOrder convertToPo(BigDecimal openPrice, BigDecimal odds) {
		SecondContractOrder po = new SecondContractOrder();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setOrderNo(po.getId());
		po.setOpenTime(LocalDateTime.now());
		po.setStatus(Constant.秒合约订单状态_进行中);
		po.setExpectCloseTime(po.getOpenTime().plusSeconds(po.getPeriod()));
		po.setAmount(this.amount);
		po.setOpenPrice(openPrice);
		po.setOdds(odds);
		return po;
	}

}
