package com.exchange.dictconfig.domain;

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

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dict_item")
@DynamicInsert(true)
@DynamicUpdate(true)
public class DictItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	private String dictItemCode;

	private String dictItemName;
	
	private Integer sortNum;
	
	@Column(name = "dict_type_id", length = 32)
	private String dictTypeId;

	/**
	 * 冗余字段：与 dict_type.dict_type_code 保持一致，便于直接查库识别归属。
	 * <p>
	 * 由 {@code DictService} 在「维护字典数据」「修改字典类型编码」时同步，不要单独赋值。
	 */
	@Column(name = "dict_type_code", length = 255)
	private String dictTypeCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dict_type_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private DictType dictType;

}
