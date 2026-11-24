package com.exchange.controller.admin.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.exchange.common.exception.BizException;
import com.exchange.common.vo.Result;
import com.exchange.storage.service.StorageService;

import cn.hutool.core.util.ArrayUtil;

@RestController
@RequestMapping("/storage")
public class StorageController {

	@Autowired
	private StorageService storageService;

	@PostMapping("/upload")
	public Result<List<String>> upload(@RequestParam("files") MultipartFile[] files) throws IOException {
		if (ArrayUtil.isEmpty(files)) {
			throw new BizException("请选择要上传的资源");
		}
		List<String> fetchUrls = new ArrayList<>();
		for (MultipartFile file : files) {
			String filename = file.getOriginalFilename();
			String fetchUrl = storageService.upload(file.getInputStream(), file.getSize(), file.getContentType(),
					filename);
			fetchUrls.add(fetchUrl);
		}
		return Result.success(fetchUrls);
	}

}
