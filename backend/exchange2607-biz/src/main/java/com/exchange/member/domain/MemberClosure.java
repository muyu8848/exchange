package com.exchange.member.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.exchange.common.utils.IdUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "member_closure")
@DynamicInsert(true)
@DynamicUpdate(true)
public class MemberClosure implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	@Column(name = "ancestor_id", length = 32)
	private String ancestorId;

	@Column(name = "descendant_id", length = 32)
	private String descendantId;

	@Column(name = "depth")
	private Integer depth;

	/**
	 * 关联会员表（只读，便于按注册时间排序）；外键复用 descendant_id 列，不参与持久化写入。
	 */
	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "descendant_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private MemberAccount descendantAccount;

	public static MemberClosure build(String ancestorId, String descendantId, int depth) {
		MemberClosure po = new MemberClosure();
		po.setId(IdUtils.getId());
		po.setAncestorId(ancestorId);
		po.setDescendantId(descendantId);
		po.setDepth(depth);
		return po;
	}

}
