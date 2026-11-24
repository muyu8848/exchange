package com.exchange.storage.backend;

import java.io.InputStream;

import org.springframework.core.io.Resource;

import com.exchange.setting.domain.OssSetting;

public interface StorageBackend {

	String getType();

	Resource loadAsResource(OssSetting setting, String id);

	void store(OssSetting setting, String id, InputStream inputStream);

	String buildPublicUrl(OssSetting setting, String id);

}
