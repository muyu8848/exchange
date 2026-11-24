package com.exchange.content.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exchange.content.domain.NoticeI18n;

public interface NoticeI18nRepo extends JpaRepository<NoticeI18n, String> {

	/** 后台编辑回填：一条公告的全部译文 */
	List<NoticeI18n> findByNoticeId(String noticeId);

	/** 客户端详情：按语言取单条译文 */
	NoticeI18n findByNoticeIdAndLangCode(String noticeId, String langCode);

	/** 客户端列表：当前页批量取译文，避免 N+1 */
	List<NoticeI18n> findByNoticeIdInAndLangCode(List<String> noticeIds, String langCode);

	/** 删除公告时清理全部译文 */
	void deleteByNoticeId(String noticeId);

	/** 编辑时清理本次未提交的语言 */
	void deleteByNoticeIdAndLangCodeNotIn(String noticeId, List<String> langCodes);

}
