package com.exchange.storage.backend;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import com.exchange.common.exception.BizException;
import com.exchange.constants.Constant;
import com.exchange.setting.domain.OssSetting;

@Component
public class LocalStorageBackend implements StorageBackend {

	@Override
	public String getType() {
		return Constant.存储类型_本地;
	}

	@Override
	public Resource loadAsResource(OssSetting setting, String id) {
		try {
			Path file = resolveFile(setting, id);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() && resource.isReadable()) {
				return resource;
			}
			return null;
		}
		catch (MalformedURLException e) {
			throw new BizException("本地文件路径无效");
		}
	}

	@Override
	public void store(OssSetting setting, String id, InputStream inputStream) {
		Path file = resolveFile(setting, id);
		try {
			Files.createDirectories(file.getParent());
			Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e) {
			throw new BizException("保存本地文件失败");
		}
	}

	@Override
	public String buildPublicUrl(OssSetting setting, String id) {
		String baseUrl = setting.getPublicFetchUrl();
		if (baseUrl.endsWith("/")) {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}
		return baseUrl + "/" + id;
	}

	private Path resolveFile(OssSetting setting, String id) {
		String localStoragePath = setting.getLocalStoragePath();
		if (localStoragePath == null || localStoragePath.trim().isEmpty()) {
			throw new BizException("本地存储目录未配置");
		}
		return Paths.get(localStoragePath.trim()).resolve(id);
	}

}
