package com.exchange.currency.param;

import java.time.LocalDateTime;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.IdUtils;
import com.exchange.currency.domain.ChainNetwork;

import lombok.Data;

@Data
public class AddOrUpdateChainNetworkParam {

	private String id;

	@NotBlank
	private String chainType;

	@NotBlank
	private String addressExplorerUrlTemplate;

	@NotBlank
	private String status;

	@NotNull
	@DecimalMin(value = "0", inclusive = true)
	private Integer sortNum;

	public ChainNetwork convertToPo() {
		ChainNetwork po = new ChainNetwork();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setCreateTime(LocalDateTime.now());
		po.setUpdateTime(po.getCreateTime());
		po.setDeletedFlag(false);
		return po;
	}
}
