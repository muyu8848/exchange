package com.exchange.storage.backend;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.exchange.common.exception.BizException;
import com.exchange.setting.domain.OssSetting;

@Component
public class StorageBackendRegistry {

	private final Map<String, StorageBackend> backendMap;

	public StorageBackendRegistry(List<StorageBackend> backends) {
		this.backendMap = backends.stream()
				.collect(Collectors.toMap(StorageBackend::getType, Function.identity()));
	}

	public StorageBackend require(OssSetting setting) {
		if (setting == null || setting.getType() == null) {
			throw new BizException("文件存储未配置");
		}
		StorageBackend backend = backendMap.get(setting.getType());
		if (backend == null) {
			throw new BizException("暂不支持该存储类型");
		}
		return backend;
	}

}
