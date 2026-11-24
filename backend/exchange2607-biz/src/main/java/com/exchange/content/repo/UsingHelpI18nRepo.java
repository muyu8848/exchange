package com.exchange.content.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exchange.content.domain.UsingHelpI18n;

public interface UsingHelpI18nRepo extends JpaRepository<UsingHelpI18n, String> {

	/** 后台编辑回填：一条帮助的全部译文 */
	List<UsingHelpI18n> findByUsingHelpId(String usingHelpId);

	/** 客户端详情：按语言取单条译文 */
	UsingHelpI18n findByUsingHelpIdAndLangCode(String usingHelpId, String langCode);

	/** 客户端列表：当前页批量取译文，避免 N+1 */
	List<UsingHelpI18n> findByUsingHelpIdInAndLangCode(List<String> usingHelpIds, String langCode);

	/** 删除帮助时清理全部译文 */
	void deleteByUsingHelpId(String usingHelpId);

	/** 编辑时清理本次未提交的语言 */
	void deleteByUsingHelpIdAndLangCodeNotIn(String usingHelpId, List<String> langCodes);

}
