package com.exchange.notification.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.common.i18n.LocaleUtils;
import com.exchange.dictconfig.DictHolder;
import com.exchange.notification.domain.MemberNotification;
import com.exchange.notification.template.NotificationTemplate;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;

/**
 * 客户端会员消息。
 */
@Data
public class MyNotificationVO {

	private String id;

	private String type;

	private String typeName;

	private String title;

	private String content;

	private String bizId;

	private Boolean readFlag;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	public static List<MyNotificationVO> convertFor(List<MemberNotification> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<MyNotificationVO> vos = new ArrayList<>();
		for (MemberNotification po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static MyNotificationVO convertFor(MemberNotification po) {
		if (po == null) {
			return null;
		}
		MyNotificationVO vo = new MyNotificationVO();
		BeanUtils.copyProperties(po, vo);
		vo.setTypeName(DictHolder.getDictItemName("memberNotificationType", po.getType()));

		if (StrUtil.isNotBlank(po.getTemplateCode())) {
			NotificationTemplate template = NotificationTemplate.of(po.getTemplateCode());
			if (template != null) {
				String langCode = LocaleUtils.currentLocale().toLanguageTag();
				Object[] args = parseParams(po.getTemplateParams());
				vo.setTitle(template.title(langCode));
				vo.setContent(template.content(langCode, template.localizeArgs(langCode, args)));
			}
		}
		return vo;
	}

	private static Object[] parseParams(String json) {
		if (StrUtil.isBlank(json)) {
			return new Object[0];
		}
		return JSONUtil.parseArray(json).toArray();
	}
}
