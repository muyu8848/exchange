package com.exchange.currency.vo;

import java.util.ArrayList;
import java.util.List;

import com.exchange.currency.domain.ChainNetwork;
import com.exchange.dictconfig.DictHolder;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class ChainNetworkVO {

	private String id;
	
	private String chainType;
	
	private String addressExplorerUrlTemplate;
	
	private String status;
	
	private String statusName;
	
	private Integer sortNum;

	public static ChainNetworkVO convertFor(ChainNetwork po) {
		if (po == null) {
			return null;
		}
		ChainNetworkVO vo = new ChainNetworkVO();
		vo.setId(po.getId());
		vo.setChainType(po.getChainType());
		vo.setAddressExplorerUrlTemplate(po.getAddressExplorerUrlTemplate());
		vo.setStatus(po.getStatus());
		vo.setStatusName(DictHolder.getDictItemName("enableStatus", po.getStatus()));
		vo.setSortNum(po.getSortNum());
		return vo;
	}

	public static List<ChainNetworkVO> convertFor(List<ChainNetwork> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<ChainNetworkVO> list = new ArrayList<>();
		for (ChainNetwork po : pos) {
			list.add(convertFor(po));
		}
		return list;
	}
}
