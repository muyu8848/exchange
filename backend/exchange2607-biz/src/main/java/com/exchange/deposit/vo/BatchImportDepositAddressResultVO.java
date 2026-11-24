package com.exchange.deposit.vo;

import lombok.Data;

@Data
public class BatchImportDepositAddressResultVO {

	private int successCount;

	private int skipExistsCount;

	private int skipDuplicateCount;

}
