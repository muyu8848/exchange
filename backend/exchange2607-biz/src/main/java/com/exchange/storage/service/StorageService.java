package com.exchange.storage.service;

import java.io.InputStream;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.exchange.common.exception.BizException;
import com.exchange.common.utils.IdUtils;
import com.exchange.setting.domain.OssSetting;
import com.exchange.setting.repo.OssSettingRepo;
import com.exchange.storage.backend.StorageBackend;
import com.exchange.storage.backend.StorageBackendRegistry;
import com.exchange.storage.domain.Storage;
import com.exchange.storage.repo.StorageRepo;
import com.exchange.storage.vo.StorageVO;

@Service
public class StorageService {

	@Autowired
	private StorageRepo storageRepo;

	@Autowired
	private OssSettingRepo ossSettingRepo;

	@Autowired
	private StorageBackendRegistry storageBackendRegistry;

	public StorageVO findById(String id) {
		return StorageVO.convertFor(storageRepo.findById(id).orElse(null));
	}

	public Resource loadAsResource(String id) {
		OssSetting ossSetting = ossSettingRepo.findTopByOrderByLatelyUpdateTime();
		StorageBackend backend = storageBackendRegistry.require(ossSetting);
		return backend.loadAsResource(ossSetting, id);
	}

	public String upload(InputStream inputStream, Long fileSize, String fileType, String fileName) {
		if (!(fileType.startsWith("image/") || fileType.startsWith("video/"))) {
			throw new BizException("只能上传图片或视频");
		}
		OssSetting ossSetting = ossSettingRepo.findTopByOrderByLatelyUpdateTime();
		StorageBackend backend = storageBackendRegistry.require(ossSetting);

		String id = IdUtils.getId();
		backend.store(ossSetting, id, inputStream);

		Storage storage = new Storage();
		storage.setId(id);
		storage.setFileName(fileName);
		storage.setFileType(fileType);
		storage.setFileSize(fileSize);
		storage.setUploadTime(LocalDateTime.now());
		storageRepo.save(storage);
		return backend.buildPublicUrl(ossSetting, storage.getId());
	}

}
