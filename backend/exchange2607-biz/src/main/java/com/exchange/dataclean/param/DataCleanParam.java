package com.exchange.dataclean.param;

import java.time.LocalDate;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class DataCleanParam {

	@NotEmpty
	private List<String> dataTypes;

	@NotNull
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate timeStart;

	@NotNull
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate timeEnd;

}
